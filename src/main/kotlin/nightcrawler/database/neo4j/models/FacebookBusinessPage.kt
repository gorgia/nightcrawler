package nightcrawler.database.neo4j.models

import org.neo4j.ogm.annotation.Relationship
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by andrea on 22/07/16.
 */
@Component
class FacebookBusinessPage : FacebookPage(){

    @Relationship(type = "LIKES", direction = Relationship.INCOMING)
    var likers: MutableSet<FacebookPage> = HashSet()

}