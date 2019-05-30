package nightcrawler.database.neo4j.models

import org.neo4j.ogm.annotation.GraphId
import org.neo4j.ogm.annotation.NodeEntity
import java.io.Serializable

/**
 * Created by andrea on 22/07/16.
 */
@NodeEntity
open class FacebookEntity: Serializable{
    //@Id
    @GraphId
    var id : Long? = null
    var url:String? = null
    var facebookId : String? = null
    var doesExist: Boolean = true
}