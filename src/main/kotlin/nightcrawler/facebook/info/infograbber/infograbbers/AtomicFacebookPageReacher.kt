package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.crawler.Crawler
import nightcrawler.crawler.commands.*
import nightcrawler.database.mongodb.models.FacebookLoginAccount
import nightcrawler.facebook.login.FacebookLoginner
import nightcrawler.springconfiguration.ApplicationContextProvider
import nightcrawler.utils.log
import org.openqa.selenium.By
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by andrea on 05/08/16.
 */
@Component
@Scope("prototype")
class AtomicFacebookPageReacher(val fla: FacebookLoginAccount? = null) {


    @Autowired
    lateinit var facebookLoginner: FacebookLoginner

    fun getPage(url: String, scrollNecessary: Boolean = false, scrollBy: Any? = null, heartbeat : Int? = null, sleepSeconds : Int? = 3, maxTimeInMinutes : Int = 10): String? {
        val crawler = ApplicationContextProvider.ctx!!.getBean(Crawler::class.java)
        synchronized(crawler) {
            if (this.fla != null) facebookLoginner.login(crawler, fla!!, true)
            else facebookLoginner.login(crawler, true)
            val commandList = LinkedList<WebDriverCommand>()
            commandList.add(CommandFactory.createCommand(GET::class.java, mapOf(Pair("url", url))))
            commandList.add(CommandFactory.createCommand(SLEEP::class.java, mapOf(Pair("seconds", sleepSeconds))))
            if (scrollNecessary || scrollBy != null) {
                var by: By? = null
                if (scrollBy is By) by = scrollBy
                else if (scrollBy is String) by = By.cssSelector(scrollBy)
                commandList.add(CommandFactory.createCommand(SCROLL::class.java, mapOf(Pair("url", url), Pair("by", by), Pair("heartbeat", heartbeat), Pair("maxTimeInMinutes", maxTimeInMinutes))))
            }
            val pageSource = try {
                crawler.invoke(commands = *commandList.toTypedArray())
            } catch (e: Exception) {
                log().error("AtomicPageReacher Error", e)
            } finally {
                Thread.sleep(500)
            }
            return pageSource as String?
        }
    }

    fun getPageWithCommands(url: String, vararg additionalCommands: WebDriverCommand): String? {
        val crawler = ApplicationContextProvider.ctx!!.getBean(Crawler::class.java)
        synchronized(crawler) {
            if (this.fla != null) facebookLoginner.login(crawler, fla!!, true)
            else facebookLoginner.login(crawler, true)
            val commandList = LinkedList<WebDriverCommand>()
            commandList.add(CommandFactory.createCommand(GET::class.java, mapOf(Pair("url", url))))
            additionalCommands.forEach { commandList.add(it) }
            val pageSource = try {
                crawler.invoke(commands = *commandList.toTypedArray())
            } catch (e: Exception) {
                log().error("AtomicPageReacher Error", e)
            } finally {
                Thread.sleep(20000)
            }
            return pageSource as String?
        }
    }


}