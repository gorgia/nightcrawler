package nightcrawler.crawler.commands

import org.openqa.selenium.By
import org.openqa.selenium.Keys

/**
 * Created by andrea on 12/12/16.
 */
class TEXT() : WebDriverCommand() {

    val by: By = By.cssSelector(params["cssSelector"] as String)
    val text: String = params["text"] as String


    override fun execute() {
        val webElement = webDriver!!.findElement(by)
        val key: Keys? = Keys.getKeyFromUnicode(text.toSortedSet().first())
        if (key != null) {
            webElement.sendKeys(text)
        } else {
            webElement.sendKeys(text)
        }
    }

}