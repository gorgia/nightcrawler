package nightcrawler.database.neo4j.repositories

import nightcrawler.database.neo4j.FindByFacebookId
import nightcrawler.database.neo4j.models.FacebookPage
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

/**
 * Created by andrea on 22/07/16.
 */
@Repository
@Service
interface FacebookPageRepository : Neo4jRepository<FacebookPage, Long> {
    fun findByFacebookIdOrUrl(facebookId: String, url: String): FacebookPage?

    @Query("match (n:FacebookPage) where n.facebookId IN {facebookIdList} return n")
    fun findByFacebookIdIn(@Param("facebookIdList") facebookIdList: List<String>): Iterable<FacebookPage>

    fun findByFacebookIdOrFacebookName(facebookId: String, facebookName: String): FacebookPage?

    fun findByFacebookName(facebookName: String): FacebookPage?

    @Query("match(n:FacebookPage) where n.facebookId IS NULL return n")
    fun findByFacebookIdNull(): List<FacebookPage>

    fun findByFacebookId(facebookId: String): FacebookPage?

    @Query("match(n:FacebookPage) where n.isTarget = true return n")
    fun findTargets(): List<FacebookPage>

}