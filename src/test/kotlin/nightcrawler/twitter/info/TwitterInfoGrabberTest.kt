package nightcrawler.twitter.info

import nightcrawler.TestConfiguration
import nightcrawler.twitter.databases.mongo.models.TwitterTarget
import nightcrawler.twitter.databases.neo4j.models.TwitterAccount
import nightcrawler.twitter.databases.neo4j.repositories.TwitterAccountRepository
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import javax.annotation.PostConstruct
import kotlin.test.assertTrue

/**
 * Created by andrea on 10/02/17.
 */

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = arrayOf("scheduling.enabled=false"), classes = arrayOf(TestConfiguration::class))
class TwitterInfoGrabberTest {


    @Autowired
    lateinit var applicationContext: ApplicationContext

    val twitterIdentifier: Long = 543279710
    var twitterTarget = TwitterTarget()

    val twitterAccount = TwitterAccount()
    lateinit var twitterInfoGrabber: TwitterInfoGrabber

    @PostConstruct
    fun init() {
        twitterTarget.twitterIdentifier = twitterIdentifier.toString()
        twitterAccount.twitterId = this.twitterIdentifier
        twitterInfoGrabber = applicationContext.getBean(TwitterInfoGrabber::class.java, twitterTarget)
    }


    @Test
    fun getNewStatuses() {
        assertTrue(twitterInfoGrabber.getNewStatuses(twitterAccount).isNotEmpty())
    }

    @Test
    @Ignore
    fun getFollowing() {
        assertTrue { twitterInfoGrabber.getFollowing(twitterAccount).isNotEmpty() }
    }

    @Test
    @Ignore
    fun getFollowers() {
        assertTrue { twitterInfoGrabber.getFollowers(twitterAccount).isNotEmpty() }
    }

    @Test
    @Ignore
    fun getLikes() {
        assertTrue { twitterInfoGrabber.getLikes(twitterAccount).isNotEmpty() }
    }

    @Test
    fun testGetQueryResult() {
        val query = "finance"
        assertTrue { twitterInfoGrabber.getInfoQuery(query).isNotEmpty() }
    }

}