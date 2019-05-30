package nightcrawler.database.neo4j.repositories

import nightcrawler.database.neo4j.models.FacebookEntity
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Created by andrea on 22/07/16.
 */
@Repository
interface FacebookEntityRepository : Neo4jRepository<FacebookEntity, Long> {
    fun findByFacebookIdOrUrl(facebookId: String, url: String): FacebookEntity

    @Query("match (n) where n.facebookId IN {facebookIdList} return n")
    fun findByFacebookIdIn(@Param("facebookIdList") facebookIdList: List<String>): Iterable<FacebookEntity>

    fun findByFacebookId(facebookId: String): FacebookEntity?

    @Query("MATCH (a:FacebookEntity) WHERE not ((a)--()) RETURN a LIMIT 500")
    fun findIsolatedNodes(): List<FacebookEntity>

    @Query("MATCH (a:FacebookEntity) WHERE id(a) = {id} DETACH DELETE a")
    fun deleteNode(@Param("id") id: Long)

    @Query("match(a:FacebookEntity) where not a:FacebookPage AND NOT a:FacebookPost return a LIMIT 1000")
    fun findWithoutLabels(): Iterable<FacebookEntity>


    @Query("match(n:FacebookPage) where n.facebookId CONTAINS \"?\" return n LIMIT 1000")
    fun findWithQuestionMark(): Iterable<FacebookEntity>

}