package nightcrawler.facebook.targetmanager

import nightcrawler.database.mongodb.models.FacebookTargetMongo
import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.database.neo4j.repositories.FacebookAccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by andrea on 22/08/16.
 * to be completed
 */
@Component
open class JarvisTargetProvider {

    @Autowired
    lateinit var facebookAccountRepository: FacebookAccountRepository


    fun getMostConnectedNotHydratedTargetsMongo(targetsToExclude: MutableList<FacebookTargetMongo>, numberOfTarget: Int = 100): List<FacebookTargetMongo> {
        val targetsIdsToExclude: HashSet<String> = HashSet()
        targetsToExclude.mapNotNullTo(targetsIdsToExclude) { it.facebookIdentifier }
        val fas: List<FacebookPage> = facebookAccountRepository.getMostConnectedNotHydratedAccounts(numberOfTarget, targetsIdsToExclude)
        return fas.map(::FacebookTargetMongo)
    }

}