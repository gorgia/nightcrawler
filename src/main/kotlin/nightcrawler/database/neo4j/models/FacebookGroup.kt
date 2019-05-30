package nightcrawler.database.neo4j.models

import org.neo4j.ogm.annotation.Relationship
import java.util.*

/**
 * Created by andrea on 22/07/16.
 */
class FacebookGroup : FacebookPage() {


    @Relationship(type = "HAS_MEMBER", direction = Relationship.OUTGOING)
    var members: MutableSet<FacebookPage> = HashSet()




}