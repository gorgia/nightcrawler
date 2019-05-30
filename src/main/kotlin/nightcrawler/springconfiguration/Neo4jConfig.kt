package nightcrawler.springconfiguration

import nightcrawler.utils.conf
import org.neo4j.ogm.session.SessionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement


/**
 * Created by andrea on 22/07/16.
 */
@Configuration
@EnableNeo4jRepositories(basePackages = ["nightcrawler.database.neo4j.repositories", "nightcrawler.twitter.databases.neo4j.repositories"])
//@EntityScan(basePackages = arrayOf("nightcrawler.database.neo4j.models", "nightcrawler.twitter.databases.neo4j.models"))
@EnableTransactionManagement
class Neo4jConfig {
    private val neo4jHttpServerAddress = conf.getString("socialnet.neo4j.httpServerAddress")

    //old configuration pre-upgrade
//    @Bean
//    fun getConfiguration(): org.neo4j.ogm.config.Configuration {
//        val config = org.neo4j.ogm.config.Configuration()
//        config.driverConfiguration().setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver").uri = neo4jHttpServerAddress
//        return config
//    }

    @Bean //ready for upgrade
    fun getConfiguration(): org.neo4j.ogm.config.Configuration {
        return org.neo4j.ogm.config.Configuration.Builder()
                .uri(conf.getString("socialnet.neo4j.boltServerAddress"))
                //.credentials("user", "secret")
                .build()
    }

    @Bean
    fun sessionFactory(): SessionFactory {
        // with domain entity base package(s)
        return SessionFactory(getConfiguration(), "nightcrawler.database.neo4j.models", "nightcrawler.twitter.databases.neo4j.models")
    }

    @Bean
    fun transactionManager(): Neo4jTransactionManager {
        return Neo4jTransactionManager(sessionFactory())
    }


    /*
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    open fun checkFacebookIdIsNotNull(facebookEntity: FacebookEntity) {
        if (facebookEntity.facebookId == null)
            throw Exception("Impossible to saveFacebookAccount facebookEntity: facebookId is missing")
    }*/
}