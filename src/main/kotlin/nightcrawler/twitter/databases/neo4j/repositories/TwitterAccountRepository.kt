package nightcrawler.twitter.databases.neo4j.repositories

import nightcrawler.twitter.databases.neo4j.models.TwitterAccount
import nightcrawler.twitter.databases.neo4j.models.TwitterStatus
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Created by andrea on 31/01/17.
 */
@Repository
interface TwitterAccountRepository : Neo4jRepository<TwitterAccount, Long> {
    fun findByTwitterId(twitterId: Long): TwitterAccount?

    fun findByName(twitterName : String): TwitterAccount?

    @Query("MATCH (following)<-[r:FOLLOWING]-(n:TwitterAccount) WHERE id(n)={twitterId} return following")
    fun getFollowing(@Param("twitterId") twitterId:Long): List<TwitterAccount>

    @Query("MATCH (follower)-[r:IS_FOLLOWED_BY]->(n:TwitterAccount) WHERE id(n)={twitterId} return follower")
    fun getFollowers(@Param("twitterId") twitterId:Long): List<TwitterAccount>

    @Query("MATCH (like)<-[r:LIKES]-(n:TwitterAccount) WHERE id(n)={twitterId} return like")
    fun getLikes(@Param("twitterId") twitterId:Long): List<TwitterStatus>
}