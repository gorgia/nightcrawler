package nightcrawler.twitter.databases.neo4j.models

import nightcrawler.database.neo4j.LocalDateTimeConverter
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.typeconversion.Convert
import java.time.LocalDateTime
import java.util.*

/**
 * Created by andrea on 31/01/17.
 */

open class TwitterAccount : TwitterEntity() {
    var name: String? = null
    var profilePic: String? = null
    var doesExists: Boolean = true
    var description: String? = null
    var email: String? = null

    @Convert(LocalDateTimeConverter::class)
    var createdAt: LocalDateTime? = null

    @Relationship(type = "FOLLOWING", direction = Relationship.OUTGOING)
    var followings: MutableSet<TwitterAccount> = HashSet<TwitterAccount>()

    @Relationship(type = "IS_FOLLOWED_BY", direction = Relationship.INCOMING)
    var followers: MutableSet<TwitterAccount> = HashSet()

    @Relationship(type = "LIKES", direction = Relationship.OUTGOING)
    var likes: MutableSet<TwitterStatus> = HashSet()

    var isGeoEnabled: Boolean = false
    var isProtected: Boolean = false
    var lang: String? = null
    var  location: String? = null
}