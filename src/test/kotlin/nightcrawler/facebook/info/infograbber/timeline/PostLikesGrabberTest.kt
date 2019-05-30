package nightcrawler.facebook.info.infograbber.timeline

import nightcrawler.TestConfiguration
import nightcrawler.database.neo4j.models.FacebookPost
import nightcrawler.database.neo4j.repositories.FacebookPostRepository
import nightcrawler.springconfiguration.ApplicationContextProvider
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import kotlin.test.assertTrue

/**
 * Created by andrea on 02/03/17.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = arrayOf("scheduling.enabled=false"), classes = arrayOf(TestConfiguration::class))
open class PostLikesGrabberTest {
    @Autowired
    lateinit var postLikesGrabber: PostLikesGrabber

    @Autowired
    lateinit var facebookPostRepository: FacebookPostRepository

    @Autowired
    lateinit var ctx: ApplicationContext

    val targetLikesUrl = "https://www.facebook.com/ufi/reaction/profile/browser/?ft_ent_identifier=10207105808197802&av=100003661309421"
    val timelineAuthorFacebookId = "100001294532736"

    @Ignore
    @Test
    open fun postLikesGrabberTest() {
        assertTrue { postLikesGrabber.extractDataFromUrl(targetLikesUrl).isNotEmpty() }
    }


    @Test
    open fun extractLikesFromTimeline() {
        val timeline = facebookPostRepository.findTimelineOfFacebookId(timelineAuthorFacebookId)
        timeline.forEach {
            val likesUrl = "https://www.facebook.com/ufi/reaction/profile/browser/?ft_ent_identifier=${it.facebookId}"
            val postLikesGrabber = ctx.getBean(PostLikesGrabber::class.java)
            val likes = postLikesGrabber.extractDataFromUrl(likesUrl)
            postLikesGrabber.updateFacebookPost(it, likes)
        }
    }

}