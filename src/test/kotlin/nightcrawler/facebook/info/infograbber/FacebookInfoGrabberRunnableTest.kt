package nightcrawler.facebook.info.infograbber

import nightcrawler.TestConfiguration
import nightcrawler.database.mongodb.models.FacebookTargetMongo
import nightcrawler.database.neo4j.models.FacebookAccount
import nightcrawler.database.neo4j.repositories.FacebookPageRepository
import nightcrawler.database.neo4j.splitLoadersAndSavers.FacebookAccountSplitLoader
import nightcrawler.facebook.info.FacebookInfoGrabberRunnable
import nightcrawler.facebook.info.FacebookNeo4jEntityFactory
import nightcrawler.facebook.targetmanager.MongoTargetManager
import nightcrawler.springconfiguration.ApplicationContextProvider
import nightcrawler.utils.log
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Future
import kotlin.test.assertNotNull


/**
 * Created by andrea on 26/07/16.
 */


@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = ["scheduling.enabled=false"], classes = [(TestConfiguration::class)])
class FacebookInfoGrabberRunnableTest {


    @Autowired
    lateinit var targetManager: MongoTargetManager

    @Autowired
    lateinit var facebookPageRepository: FacebookPageRepository

    @Autowired
    lateinit var facebookAccountSplitLoader: FacebookAccountSplitLoader

    val targetsId = arrayOf("1745360740")

    @Autowired
    lateinit var infoGrabberExecutor: ThreadPoolTaskExecutor

    @Test
    fun testInfoGrabber() {
        val futures = LinkedList<Future<*>>()
        infoGrabberExecutor.setWaitForTasksToCompleteOnShutdown(true)
        targetsId.forEach { targetId ->
            log().info("Processing Target: $targetId")
            var fp = facebookPageRepository.findByFacebookIdOrFacebookName(targetId, targetId)
            if (fp is FacebookAccount)
                fp = facebookAccountSplitLoader.load(fp)
            var target: FacebookTargetMongo? = targetManager.getOrCreateTargetFromFacebookIdentifer(targetId)
            if (target == null && fp != null) {
                target = targetManager.getOrCreateTargetFromFacebookIdentifer(fp.facebookId!!)
            }
            target!!.priority = 1
            target.eagerTimeline = true
            target.saveInDb = false
            futures.add(infoGrabberExecutor.submit { (ApplicationContextProvider.ctx?.getBean(FacebookInfoGrabberRunnable::class.java, target) as FacebookInfoGrabberRunnable).call() })
            log().info("testIn" +
                    "foGrabber in process for : $targetId")
        }

        infoGrabberExecutor.shutdown()
        for (future in futures) {
            future.get()
        }

    }
}