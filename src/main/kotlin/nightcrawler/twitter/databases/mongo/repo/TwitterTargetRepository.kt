package nightcrawler.twitter.databases.mongo.repo

import nightcrawler.twitter.databases.mongo.models.TwitterLoginAccount
import nightcrawler.twitter.databases.mongo.models.TwitterTarget
import nightcrawler.twitter.databases.neo4j.models.TwitterAccount
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * Created by andrea on 31/01/17.
 */
@Repository
interface TwitterTargetRepository : MongoRepository<TwitterTarget, String> {
}
