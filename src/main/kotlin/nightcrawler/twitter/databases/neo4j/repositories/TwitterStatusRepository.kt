package nightcrawler.twitter.databases.neo4j.repositories

import nightcrawler.twitter.databases.neo4j.models.TwitterStatus
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Created by andrea on 01/02/17.
 */
@Repository
interface TwitterStatusRepository : Neo4jRepository<TwitterStatus, Long> {
    fun findByTwitterId(twitterId: Long): TwitterStatus?

    @Query("MATCH (n:TwitterStatus) " +
            "WHERE  n.twitterId IN {idsToExclude} " +
            "RETURN n ")
    fun findByTwitterIdIn(@Param("twitterIds") twitterIds: Iterable<Long>): List<TwitterStatus>
}