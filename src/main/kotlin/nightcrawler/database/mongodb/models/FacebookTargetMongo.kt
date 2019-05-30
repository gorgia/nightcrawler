package nightcrawler.database.mongodb.models

import nightcrawler.database.neo4j.models.FacebookPage
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.stereotype.Component
import java.io.Serializable
import java.time.LocalDateTime

/**
 * Created by andrea on 27/07/16.
 */
@Component
@Document
class FacebookTargetMongo() : Comparable<FacebookTargetMongo>, Serializable {
    @Id
    var facebookIdentifier: String? = null
    var neo4jNodeId: Long? = null
    var creationDate: LocalDateTime = LocalDateTime.now()
    var eagerComments: Boolean = false
    var eagerTimeline: Boolean = false
    var downloadTimeline: Boolean = true
    var saveInDb: Boolean = true
    var doesExist: Boolean = false
    var mailOnCompletion: Boolean = false
    var updateMailList: MutableSet<String>? = null
    var updateTimelineEveryMinutes: Long = 7200
    var updateAttributesEveryDays: Long = 60
    var basicInfoOnly = false
    var lastUpdateTimeline: LocalDateTime? = null
    var lastUpdateAttributes: LocalDateTime? = null
    var priority : Int = 0
    var facebookTargetMongoMissionControl: FacebookTargetMongoMissionControl? = null

    override fun compareTo(other: FacebookTargetMongo): Int {
        return if (this.facebookIdentifier.equals(other.facebookIdentifier)) 0
        else 1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as FacebookTargetMongo
        if (facebookIdentifier != other.facebookIdentifier) return false
        return true
    }

    override fun hashCode(): Int {
        return facebookIdentifier?.hashCode() ?: 0
    }

    constructor(fa: FacebookPage) : this() {
        this.facebookIdentifier = fa.facebookName
        this.neo4jNodeId = fa.id
    }
}