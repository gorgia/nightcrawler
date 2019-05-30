package nightcrawler.crawler.commands

import nightcrawler.utils.log
import org.openqa.selenium.By

/**
 * Created by andrea on 12/12/16.
 */
class CLICK : WebDriverCommand() {

    override fun execute() {
        val by: By? = params["by"] as By?
        var cycle: Int = params["cycle"] as Int? ?: 1
        var waitSeconds: Int = params["waitSeconds"] as Int? ?: 3
        waitForPageToLoad()
        try {
            do {
                var elementsToClick = webDriver!!.findElements(by)
                if (elementsToClick.isEmpty()) return
                elementsToClick = elementsToClick.filter { it.isDisplayed }
                if (elementsToClick.isEmpty()) return
                elementsToClick.first().click()
                waitSeconds(waitSeconds)
                waitForPageToLoad()
                if (cycle > 0) cycle--
            } while (cycle > 0 && elementsToClick.isNotEmpty())
        } catch(e: Exception) {
            log().error("click exception catch. to be removed", e)
        }
    }

}