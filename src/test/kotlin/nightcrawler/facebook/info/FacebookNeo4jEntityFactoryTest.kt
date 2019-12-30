package nightcrawler.facebook.info

import nightcrawler.TestConfiguration
import nightcrawler.database.neo4j.models.FacebookAccount
import nightcrawler.database.neo4j.models.FacebookPost
import nightcrawler.database.neo4j.repositories.FacebookPageRepository
import nightcrawler.database.neo4j.splitLoadersAndSavers.FacebookAccountSplitLoader
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import kotlin.test.assertNotNull

/**
 * Created by andrea on 18/08/16.
 */

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = arrayOf("scheduling.enabled=false"), classes = arrayOf(TestConfiguration::class))
class FacebookNeo4jEntityFactoryTest {
    @Autowired
    lateinit var facebookEntityFactory: FacebookNeo4jEntityFactory

    @Autowired
    lateinit var facebookPageRepo: FacebookPageRepository

    @Autowired
    lateinit var facebookAccountSplitLoader: FacebookAccountSplitLoader

    var facebookId = "facebookIdHere"

    var facebookEntityClass = FacebookPost::class.java

    @Test
    @Ignore
    fun getFacebookEntity() {
        val facebookEntity = facebookEntityFactory.getByFacebookId(facebookId, facebookEntityClass, true)
        assertNotNull(facebookEntity)
    }

    @Test
    fun getFacebookPage() {
        val facebookPage = facebookPageRepo.findByFacebookIdOrFacebookName(facebookId, facebookId)
        assertNotNull(facebookPage)
    }

    @Test
    fun saveFacebookAccount() {
        var facebookAccount : FacebookAccount = facebookEntityFactory.getByFacebookId("renatoderosa", clazz = FacebookAccount::class.java)!!
        facebookAccount = facebookAccountSplitLoader.load(facebookAccount)
        facebookEntityFactory.saveFacebookAccount(facebookAccount)
    }


}