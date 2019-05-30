package nightcrawler.database.neo4j.repositories

import nightcrawler.database.neo4j.FindByFacebookId
import nightcrawler.database.neo4j.models.FacebookPost
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Created by andrea on 26/07/16.
 */
@Repository
interface FacebookPostRepository : Neo4jRepository<FacebookPost, Long> {
    fun findByFacebookIdOrUrl(facebookId: String, url: String): FacebookPost
    fun findByFacebookId(facebookId: String): FacebookPost?

    @Query("match(p:FacebookPost)<-[:WRITTEN_BY]-(n:FacebookPage) where n.facebookId = {facebookId} return p")
    fun findTimelineOfFacebookId(@Param("facebookId") facebookId: String): List<FacebookPost>
}