package nightcrawler.database.neo4j.splitLoadersAndSavers

/**
 * Created by andrea on 24/10/16.
 */

import nightcrawler.utils.conf
import org.springframework.stereotype.Component
import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.ClientResponse
import com.sun.jersey.api.client.WebResource
import nightcrawler.utils.log
import org.neo4j.driver.v1.*
import javax.ws.rs.core.MediaType

@Component
open class RestApiNeo4jClient {

    val serverRootUri: String = conf.getString("socialnet.neo4j.httpServerAddress")
    val driver: Driver = GraphDatabase.driver(conf.getString("socialnet.neo4j.boltServerAddress"))


    open fun sendCypherOld(cypherQuery: String, params: Map<String, Any>) {
        var payload: String = ""
        try {
            val resourceUri = "db/data/transaction/commit";
            val txUri = serverRootUri + "/" + resourceUri;
            val resource: WebResource = Client.create().resource(txUri);
            val mapper: ObjectMapper = ObjectMapper()
            payload = "{\"statements\" : [ {\"statement\" : \"$cypherQuery\", \"parameters\" : ${mapper.writeValueAsString(params)} } ]}";
            log().debug("Sending payload : \n $payload")


            var response = resource
                    .accept(MediaType.APPLICATION_JSON)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(payload)
                    .post(ClientResponse::class.java);

            log().debug("Response:\n $response")

            response.close()
        } catch(e: Exception) {
            log().error("Exception during sending of cypher to neo4j http transaction endpoint. Payload: \n $payload", e)
        }
    }


    open fun sendCypher(cypherQuery: String, params: Map<String, Any>): StatementResult? {
        val session: Session = driver.session()
        val rs: StatementResult?
        try {
            val statementTemplate = Statement(cypherQuery, params)
            rs = session.run(statementTemplate)
        } finally {
            session.close()
        }
        return rs
    }

}