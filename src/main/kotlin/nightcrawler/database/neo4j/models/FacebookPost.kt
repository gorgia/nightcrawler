package nightcrawler.database.neo4j.models

import nightcrawler.database.neo4j.service.FacebookPostType
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.typeconversion.DateLong
import java.util.*

/**
 * Created by andrea on 22/07/16.
 */

class FacebookPost : FacebookEntity() {

    var facebookPostType: FacebookPostType = FacebookPostType.POST

    var text: String? = null

    @DateLong
    var date: Date? = null

    @Relationship(type = "WRITTEN_BY", direction = Relationship.INCOMING)
    var author: FacebookPage? = null

    @Relationship(type = "LIKES", direction = Relationship.INCOMING)
    var likes: MutableSet<FacebookPage> = HashSet()

    @Relationship(type = "HAS_COMMENT", direction = Relationship.OUTGOING)
    var comments: MutableSet<FacebookPost> = HashSet()

    @Relationship(type = "SHARED_BY", direction = Relationship.OUTGOING)
    var shares: MutableSet<FacebookPost> = HashSet()

    var images: MutableSet<String>? = null

}