package nightcrawler.twitter.databases.mongo.models

import nightcrawler.TestConfiguration
import nightcrawler.twitter.databases.mongo.TwitterTargetManager
import nightcrawler.twitter.databases.mongo.repo.TwitterTargetRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import kotlin.test.assertNotNull

/**
 * Created by andrea on 15/02/17.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = arrayOf("scheduling.enabled=false"), classes = arrayOf(TestConfiguration::class))
open class TwitterTargetTest {


    @Autowired
    lateinit var twitterTargetRepo: TwitterTargetRepository

    @Autowired
    lateinit var twitterTargetManager: TwitterTargetManager

    @Test
    open fun testSave() {
        val queryString = "cinzia dalla porta"
        val twitterTarget = twitterTargetManager.getOrCreateTargetFromTwitterIdentifier(twitterIdentifier = queryString, isQuery = true, saveInDb = true)
        assertNotNull(twitterTarget)
    }
}