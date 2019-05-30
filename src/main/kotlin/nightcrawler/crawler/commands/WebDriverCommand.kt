package nightcrawler.crawler.commands

import com.google.common.base.Predicate
import nightcrawler.utils.log
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.FluentWait
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by andrea on 22/07/16.
 */
abstract class WebDriverCommand : Command {
    var webDriver: WebDriver? = null
    var params: Map<String, Any?> = HashMap()

    protected open fun waitForPageToLoad() {
        try {
            val wait = FluentWait<WebDriver>(webDriver).withTimeout(30, TimeUnit.SECONDS)
                    .pollingEvery(5, TimeUnit.SECONDS)
                    .ignoring(NoSuchElementException::class.java)
            wait.until({ (webDriver as JavascriptExecutor).executeScript("return document.readyState;") == "complete" })
        } catch (e: Exception) {
            log().error("Unable wait for page during command ${this::class.java}")
        }
    }

    open fun waitSeconds(seconds: Int) {
        if (seconds > 0) Thread.sleep(seconds * 1000.toLong())
    }
}