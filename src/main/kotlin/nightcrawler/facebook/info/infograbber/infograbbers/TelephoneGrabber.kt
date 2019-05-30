package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.crawler.Crawler
import nightcrawler.crawler.commands.*
import nightcrawler.crawler.webdriver.findElementsWithFuentWait
import nightcrawler.crawler.webdriver.takeScreenshot
import nightcrawler.database.mongodb.models.FacebookLoginAccount
import nightcrawler.facebook.info.extractFacebookNameFromHref
import nightcrawler.facebook.info.sanitizeUrltoPath
import nightcrawler.facebook.login.FacebookLoginner
import nightcrawler.facebook.targetmanager.MongoTargetManager
import nightcrawler.springconfiguration.ApplicationContextProvider
import nightcrawler.utils.log
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.openqa.selenium.By
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.io.File
import java.util.*

/**
 * Created by andrea on 23/12/16.
 */
@Component
@Scope("prototype")
class TelephoneGrabber(var targetTelephone: String, var saveInTargetRepositoryIfFound: Boolean = true, override val fla:FacebookLoginAccount? = null) : AtomicFacebookPageReacher(fla), java.util.concurrent.Callable<String?> {


    @Autowired
    private lateinit var mongoTargetManager: MongoTargetManager


    override fun call(): String? {
            val facebookId = this.process()
            if (facebookId != null) {
                log().info("facebookId: $facebookId found for telephone: $targetTelephone")
                if (saveInTargetRepositoryIfFound) mongoTargetManager.getOrCreateTargetFromFacebookIdentifer(facebookId, saveInDb = true)
            }
            if(facebookId != null)
                printToFile(targetTelephone, facebookId)
            return facebookId
    }

    fun process(): String? {
        targetTelephone = targetTelephone.replace("+", "00")
        val pageHtml = this.getPageWithCommands("https://www.facebook.com/search/people/?q=$targetTelephone") ?: return null
        val doc = Jsoup.parse(pageHtml)
        val browseResultContainer = doc.getElementById("BrowseResultsContainer")
        if(browseResultContainer == null) log().warn("No account found for telephone: $targetTelephone")
        else {
            val href = browseResultContainer.getElementsByAttributeValueStarting("href","https://www.facebook.com/").first().attr("href")
            return extractFacebookNameFromHref(href)!!
        }
        return null
    }

    fun getPageWithCommands(url: String): String?{
        val commandsList: LinkedList<WebDriverCommand> = LinkedList()
        commandsList.add(CommandFactory.createCommand(SLEEP::class.java, mapOf(Pair ("seconds", 20))))
        commandsList.add(CommandFactory.createCommand(SCREENSHOT::class.java, mapOf(Pair("fileName", "telephoneGrabber/${sanitizeUrltoPath(url)}"))))
        return getPageWithCommands(url, *commandsList.toTypedArray())
    }



    @Synchronized
    fun printToFile(telephone: String, facebookId: String) {
        File("outputTelephoneGrabber.txt").appendText(telephone + " - " + facebookId + "\n")
    }
}