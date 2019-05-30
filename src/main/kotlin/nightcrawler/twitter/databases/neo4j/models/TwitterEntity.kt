package nightcrawler.twitter.databases.neo4j.models

import org.neo4j.ogm.annotation.GraphId
import org.neo4j.ogm.annotation.NodeEntity
import java.io.Serializable

/**
 * Created by andrea on 31/01/17.
 */
@NodeEntity
open class TwitterEntity: Serializable {
    @GraphId var id: Long? = null
    var twitterId: Long? = null
}