package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.TestConfiguration
import nightcrawler.database.mongodb.models.FacebookTargetMongoMissionControl
import nightcrawler.database.neo4j.models.FacebookBusinessPage
import nightcrawler.database.neo4j.repositories.FacebookBusinessPageRepository
import nightcrawler.facebook.info.FacebookInfoGrabberRunnable
import nightcrawler.facebook.info.FacebookNeo4jEntityFactory
import nightcrawler.facebook.targetmanager.MongoTargetManager
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.util.concurrent.TimeUnit


@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = arrayOf("scheduling.enabled=false"), classes = arrayOf(TestConfiguration::class))
class BusinessPageTest {

    val testPageId = "casapounditalia"

    @Autowired
    private lateinit var ctx: ApplicationContext

    @Autowired
    private lateinit var facebookTargetManager: MongoTargetManager

    @Autowired
    lateinit var infoGrabberExecutor: ThreadPoolTaskExecutor

    @Autowired
    lateinit var neo4jEntityFactory: FacebookNeo4jEntityFactory

    @Test
    fun test() {
        val ft = facebookTargetManager.getOrCreateTargetFromFacebookIdentifer(testPageId)
        ft.facebookTargetMongoMissionControl = FacebookTargetMongoMissionControl("test", 1, ft.facebookIdentifier)
        ft.priority = 1
        ft.saveInDb = false
        val infoGrabberExecutor = ctx.getBean(FacebookInfoGrabberRunnable::class.java, ft)
        val fp = neo4jEntityFactory.getOrCreateFacebookPageFromTarget(ft, FacebookBusinessPage::class.java)
        infoGrabberExecutor.extractBusinessPageAttributes(ft, fp)
    }
}