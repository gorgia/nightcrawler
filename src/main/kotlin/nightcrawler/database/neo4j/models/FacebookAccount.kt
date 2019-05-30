package nightcrawler.database.neo4j.models

import org.neo4j.ogm.annotation.Relationship
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by andrea on 22/07/16.
 */
@Component
class FacebookAccount : FacebookPage() {
    @Relationship(type = "LIVES_IN", direction = Relationship.OUTGOING)
    var livesin: FacebookPage? = null

    @Relationship(type = "HAS_LIVED_IN", direction = Relationship.OUTGOING)
    var hasLivedIn: MutableSet<FacebookPage> = HashSet()

    @Relationship(type = "HAS_EXPERIENCE", direction = Relationship.OUTGOING)
    var experience: MutableSet<FacebookPage> = HashSet()

    @Relationship(type = "HAS_AS_FRIEND", direction = Relationship.OUTGOING)
    var friends: MutableSet<FacebookPage> = HashSet()

    @Relationship(type = "FOLLOWING", direction = Relationship.OUTGOING)
    var following: MutableSet<FacebookPage> = HashSet()

    @Relationship(type = "FOLLOWING", direction = Relationship.INCOMING)
    var followers: MutableSet<FacebookPage> = HashSet()

}