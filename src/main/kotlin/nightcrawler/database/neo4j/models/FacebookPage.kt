package nightcrawler.database.neo4j.models

import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.typeconversion.DateLong
import java.util.*

/**
 * Created by andrea on 22/07/16.
 */

open class FacebookPage : FacebookEntity() {
    var profilePic: String? = null
    var coverPhotoImg: String? = null
    var facebookName: String? = null
    var name: String? = null
    var isTarget: Boolean? = null


    @Relationship(type = "LIKES", direction = Relationship.OUTGOING)
    var likes: MutableSet<FacebookPage> = HashSet()

    @DateLong
    var hydrationDate: Date? = null
}