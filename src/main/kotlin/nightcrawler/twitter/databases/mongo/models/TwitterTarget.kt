package nightcrawler.twitter.databases.mongo.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

/**
 * Created by andrea on 31/01/17.
 */
@Document
open class TwitterTarget {
    @Id
    var twitterIdentifier: String? = null
    var neo4jNodeId: Long? = null
    var creationDate: LocalDateTime = LocalDateTime.now()
    var updateTimelineEveryMinutes: Long = 7200
    var updateAttributesEveryDays: Long = 60
    var lastUpdateTimeline: LocalDateTime? = null
    var lastUpdateAttributes: LocalDateTime? = null
    var mailOnCompletion: Boolean = false
    var updateMailList: MutableSet<String> = HashSet()
    var priority: Int = 0
    var doesExist: Boolean = false
    var isQuery: Boolean = false


}