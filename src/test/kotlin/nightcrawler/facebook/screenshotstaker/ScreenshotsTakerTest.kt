package nightcrawler.facebook.screenshotstaker

import com.assertthat.selenium_shutterbug.core.Shutterbug
import com.assertthat.selenium_shutterbug.utils.file.FileUtil
import com.assertthat.selenium_shutterbug.utils.web.ScrollStrategy
import com.google.common.io.Resources
import nightcrawler.ApplicationNightcrawler
import nightcrawler.crawler.Crawler
import nightcrawler.crawler.webdriver.takeScreenshot
import nightcrawler.database.mongodb.repositories.FacebookTargetRepository
import nightcrawler.database.neo4j.models.FacebookAccount
import nightcrawler.database.neo4j.repositories.FacebookAccountRepository
import nightcrawler.facebook.info.FacebookInfoGrabberRunnable
import nightcrawler.facebook.info.baseUrlFromFacebookName
import nightcrawler.facebook.info.infograbber.infograbbers.*
import nightcrawler.facebook.info.infograbber.timeline.TimelineGrabber
import nightcrawler.facebook.login.FacebookLoginner
import nightcrawler.springconfiguration.ApplicationContextProvider
import nightcrawler.utils.log
import org.junit.Test
import org.junit.runner.RunWith
import org.openqa.selenium.By
import org.openqa.selenium.remote.RemoteWebDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File
import java.util.*

/**
 * Created by andrea on 28/12/16.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(ApplicationNightcrawler::class))
class ScreenshotsTakerTest {


    val filename = "output-old.txt"

    @Autowired
    lateinit private var ctx: ApplicationContext

    @Autowired
    lateinit var infoGrabberExecutor: ThreadPoolTaskExecutor

    @Autowired
    lateinit var facebookAccountRepository: FacebookAccountRepository

    @Autowired
    lateinit var facebookTargetRepository: FacebookTargetRepository

    @Autowired
    lateinit private var facebookLoginner: FacebookLoginner

    @Test
    fun process() {
        obtainList()
    }


    fun obtainList() {
        val url = Resources.getResource(filename)
        val uri = url.toURI()
        val file = File(uri)
        val lines = file.readLines()
        lines.forEach { line ->
            val split = line.split(" - ")
            val telephone = split[0]
            val facebookId = split[1]
            var next = false
            do {
                try {
                    next = false
                    if (infoGrabberExecutor.activeCount < infoGrabberExecutor.corePoolSize) {
                        infoGrabberExecutor.execute({ takeScreenShot(telephone, facebookId) })
                        next = true
                    }
                    if (!next) Thread.sleep(3000)
                } catch(e: Exception) {
                    log().error("error during elaboration of facebookId : $facebookId", e)
                }
            } while (!next)
        }
        Thread.sleep(1000000)
    }


    fun takeScreenShot(telephone: String, facebookId: String) {
        val crawler = ApplicationContextProvider.ctx!!.getBean(Crawler::class.java)
        try {
            val fa = facebookAccountRepository.findByFacebookId(facebookId)
            if (fa == null) {
                log().warn("FacebookId:$facebookId not found.")
                return
            }
            log().info("Trying to take screenshot of:${fa.facebookId} - ${fa.facebookName}")
            val baseFolder = "telephones/$telephone -  ${fa.facebookId} - ${fa.facebookName} - ${fa.name}/"
            try {
                ctx.getBean(TimelineGrabber::class.java, crawler, baseUrlFromFacebookName(fa.facebookName!!)).process(eagerTimeline = true)
            } catch(e: Exception) {
                log().error("", e)
                (crawler.webDriver as RemoteWebDriver).sessionId == null
                try {
                    crawler.close()
                } catch(z: Exception) {
                    log().error("Impossible to close RemoteWebDriver.", z)
                }
                return
            }
            val elementToBlur = crawler.webDriver.findElement(By.xpath("//div[@role=\"navigation\"]"))
            crawler.webDriver.takeScreenshot(baseFolder + "page.png", fullpage = true, elementsToBlur = elementToBlur)
        } catch(e: Exception) {
            log().info("Exception", e)
        } finally {
            Thread.sleep(10000)
            crawler.close()
        }
    }


}