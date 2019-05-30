package nightcrawler.facebook.info.infograbber.infograbbers

import com.google.common.io.Resources
import nightcrawler.TestConfiguration
import nightcrawler.facebook.login.FacebookLoginAccountManager
import nightcrawler.utils.log
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Created by andrea on 23/12/16.
 */

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = arrayOf("scheduling.enabled=false"), classes = arrayOf(TestConfiguration::class))
class TelephoneGrabberTest {


    val filename = "telefoni-11-2017"

    var singleNumber = "393286484744"

    @Autowired
    lateinit private var ctx: ApplicationContext

    @Autowired
    lateinit var infoGrabberExecutor: ThreadPoolTaskExecutor

    @Autowired
    lateinit var flam: FacebookLoginAccountManager

    @Test
    fun process() {
        obtainList()
    }

    fun obtainList() {
        val url = Resources.getResource(filename)
        val uri = url.toURI()
        val file = File(uri)
        val lines = file.readLines()
        infoGrabberExecutor.setWaitForTasksToCompleteOnShutdown(true)
        val futures = LinkedList<Future<*>>()

        lines.forEach { line ->
            futures.add(infoGrabberExecutor.submit(ctx.getBean(TelephoneGrabber::class.java, line, true, flam.getRandomTelephoneNumberFinderEnabledAccount())))
        }
        for (future in futures) {
            val facebookId = future.get()
            if (facebookId != null)
                log().info("found facebookId from telephone: $facebookId")
        }
        infoGrabberExecutor.shutdown()
    }


    @Test
    @Ignore
    fun processSingleNumber() {
        val exService: ExecutorService = Executors.newSingleThreadExecutor()
        exService.submit(ctx.getBean(TelephoneGrabber::class.java, singleNumber, true))
        exService.awaitTermination(200, TimeUnit.SECONDS)
    }


}
