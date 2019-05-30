package nightcrawler.crawler.commands

import nightcrawler.crawler.webdriver.takeScreenshot
import nightcrawler.utils.log
import org.openqa.selenium.By

/**
 * Created by andrea on 12/12/16.
 */
class SCREENSHOT : WebDriverCommand() {

    override fun execute() {
        val fileFolder : String? = params["fileFolder"] as String?
        var fileName: String = params["fileName"] as String? ?: "snap"
        if(!fileName.endsWith(".jpg")) fileName += ".jpg"
         waitForPageToLoad()
        try {
                val filePath = if(fileFolder!=null) "$fileFolder/$fileName" else fileName
                webDriver?.takeScreenshot(filePath)
        } catch(e: Exception) {
            log().error("screenshot exception catch. to be removed", e)
        }
    }

}