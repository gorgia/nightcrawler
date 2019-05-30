package nightcrawler.database.neo4j.repositories

import nightcrawler.database.neo4j.FindByFacebookId
import nightcrawler.database.neo4j.models.FacebookBusinessPage
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
interface FacebookBusinessPageRepository : Neo4jRepository<FacebookBusinessPage, Long> {
    fun findByFacebookIdOrUrl(facebookId: String, url: String): FacebookBusinessPage?

    @Query("match (n:FacebookBusinessPage) where n.facebookId IN {facebookIdList} return n")
    fun findByFacebookIdIn(@Param("facebookIdList") facebookIdList: List<String>): Iterable<FacebookBusinessPage>

    fun findByFacebookIdOrFacebookName(facebookId: String, facebookName: String): FacebookBusinessPage?

    fun findByFacebookName(facebookName: String): FacebookBusinessPage?

    @Query("match(n:FacebookBusinessPage) where n.facebookId IS NULL return n")
    fun findByFacebookIdNull(): List<FacebookBusinessPage>

    fun findByFacebookId(facebookId: String): FacebookBusinessPage?

    @Query("match(n:FacebookBusinessPage) where n.isTarget = true return n")
    fun findTargets(): List<FacebookBusinessPage>

    @Query("match(p:FacebookPage)-[LIKES]->(n:FacebookBusinessPage) where n.facebookId = {facebookId} return p")
    fun findLikers(@Param("facebookId") facebookId: String): List<FacebookBusinessPage>

}