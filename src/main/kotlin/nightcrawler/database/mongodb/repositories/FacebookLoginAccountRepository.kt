package nightcrawler.database.mongodb.repositories

import nightcrawler.database.mongodb.models.FacebookLoginAccount
import org.springframework.context.annotation.Scope
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * Created by andrea on 26/07/16.
 */
@Repository
@Scope("singleton")
interface FacebookLoginAccountRepository : MongoRepository<FacebookLoginAccount, String> {
}