package nightcrawler.database.neo4j.repositories

import nightcrawler.database.neo4j.FindByFacebookId
import nightcrawler.database.neo4j.models.FacebookGroup
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
interface FacebookGroupRepository : Neo4jRepository<FacebookGroup, Long> {
    fun findByFacebookIdOrUrl(facebookId: String, url: String): FacebookGroup
    @Query("match (n) where n.facebookId IN {facebookIdList} return n")
    fun findByFacebookIdIn(@Param("facebookIdList") facebookIdList: List<String>): Iterable<FacebookGroup>
    fun findByFacebookId(facebookId: String): FacebookGroup?

}