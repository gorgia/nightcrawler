package nightcrawler.database.neo4j.repositories

import nightcrawler.database.neo4j.FindByFacebookId
import nightcrawler.database.neo4j.models.FacebookAccount
import nightcrawler.database.neo4j.models.FacebookPage
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Created by andrea on 22/07/16.
 */
@Repository
interface FacebookAccountRepository : Neo4jRepository<FacebookAccount, Long> {

    fun findByFacebookName(facebookName: String): FacebookAccount?
    fun findByFacebookIdOrFacebookName(facebookId: String, facebookName: String): FacebookAccount?
    fun findByFacebookId(facebookId: String): FacebookAccount?

    @Query("MATCH (n:FacebookAccount)--(c) " +
            "with n, count(c) AS connections " +
            "ORDER BY connections  DESC " +
            "WHERE NOT n.facebookId IN {idsToExclude} " +
            "AND n.hydrationDate is null " +
            "RETURN n " +
            "LIMIT {limit}")
    fun getMostConnectedNotHydratedAccounts(@Param("limit") limit: Int, @Param("idsToExclude") idsToExclude: Iterable<String>): List<FacebookAccount>


    @Query("MATCH (friend)<-[r:HAS_AS_FRIEND]-(n:FacebookPage) WHERE id(n)={id} return friend")
    fun getFriends(@Param("id") id:Long): List<FacebookPage>

    @Query("MATCH (follower)-[r:IS_FOLLOWED_BY]->(n:FacebookPage) WHERE id(n)={id} return follower")
    fun getFollowers(@Param("id") id:Long): List<FacebookPage>

    @Query("MATCH (following)<-[r:FOLLOWING]-(n:FacebookPage) WHERE id(n)={id} return following")
    fun getFollowing(@Param("id") id:Long): List<FacebookPage>

    @Query("MATCH (like)<-[r:LIKES]-(n:FacebookPage) WHERE id(n)={id} return like")
    fun getLikes(@Param("id") id:Long): List<FacebookPage>

    @Query("MATCH (experience)<-[r:HAS_EXPERIENCE]-(n:FacebookPage) WHERE id(n)={id} return experience")
    fun getExperience(@Param("id") id:Long): List<FacebookPage>

    @Query("MATCH (hasLivedIn)<-[r:HAS_LIVED_IN]-(n:FacebookPage) WHERE id(n)={id} return hasLivedIn")
    fun getHasLivedIn(@Param("id") id:Long): List<FacebookPage>

    @Query("MATCH (livesIn)<-[r:LIVES_IN]-(n:FacebookPage) WHERE id(n)={id} return livesIn")
    fun getLivesIn(@Param("id") id:Long): FacebookPage?
}