package nightcrawler.database.neo4j.splitLoadersAndSavers

import nightcrawler.database.neo4j.models.FacebookAccount
import nightcrawler.database.neo4j.repositories.FacebookAccountRepository
import nightcrawler.utils.log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by andrea on 14/09/16.
 */

@Component
class FacebookAccountSplitLoader {

    @Autowired
    lateinit var facebookAccountRepository: FacebookAccountRepository


    fun load(fa: FacebookAccount): FacebookAccount {
        if (fa.id == null) return fa
        val neo4jNodeId: Long = fa.id!!
        try {
            fa.friends = facebookAccountRepository.getFriends(neo4jNodeId).toMutableSet()
            fa.followers = facebookAccountRepository.getFollowers(neo4jNodeId).toMutableSet()
            fa.following = facebookAccountRepository.getFollowing(neo4jNodeId).toMutableSet()
            fa.experience = facebookAccountRepository.getExperience(neo4jNodeId).toMutableSet()
            fa.likes = facebookAccountRepository.getLikes(neo4jNodeId).toMutableSet()
            fa.hasLivedIn = facebookAccountRepository.getHasLivedIn(neo4jNodeId).toMutableSet()
            fa.livesin = facebookAccountRepository.getLivesIn(neo4jNodeId)
        } catch(e: Exception) {
            log().error("Load exception for ${fa.facebookName} fid: ${fa.facebookId} id: ${fa.id}", e)
        }
        return fa
    }


}