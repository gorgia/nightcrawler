package nightcrawler.database.mongodb.repositories

import nightcrawler.database.mongodb.models.FacebookTargetMongo
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * Created by andrea on 27/07/16.
 */
@Repository
interface FacebookTargetRepository : MongoRepository<FacebookTargetMongo, String> {
    fun findByNeo4jNodeId(neo4jNodeId : Long): FacebookTargetMongo

    fun findByPriorityGreaterThan(priority : Int = 0): List<FacebookTargetMongo>



}
