package nightcrawler.twitter.databases.neo4j.models

import nightcrawler.database.neo4j.LocalDateTimeConverter
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.typeconversion.Convert
import java.time.LocalDateTime
import java.util.*

/**
 * Created by andrea on 31/01/17.
 */
open class TwitterStatus : TwitterEntity(){

    @Convert(LocalDateTimeConverter::class)
    var createdAt : LocalDateTime? = null

    var retweetCount: Int = 0

    var text : String? = null

    @Relationship(type = "WRITTEN_BY", direction = Relationship.INCOMING)
    var author : TwitterAccount? = null

    @Relationship(type = "IS_CONTRIBUTOR_OF", direction = Relationship.INCOMING)
    var contributors : Set<TwitterAccount> = HashSet()

    @Relationship(type = "IN_REPLY_TO_STATUS", direction = Relationship.OUTGOING)
    var inReplyToStaus : TwitterStatus? = null

    @Relationship(type = "IN_REPLY_TO_USER", direction = Relationship.OUTGOING)
    var inReplyToUser : TwitterAccount? = null

    @Relationship(type = "IS_RETWEET_OF", direction = Relationship.OUTGOING)
    var retweetedStatus : TwitterStatus? = null

    @Relationship(type = "QUOTES", direction = Relationship.OUTGOING)
    var quotedStatus : TwitterStatus? = null



}