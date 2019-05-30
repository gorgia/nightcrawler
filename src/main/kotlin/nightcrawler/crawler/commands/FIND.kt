package nightcrawler.crawler.commands

import com.google.common.base.Function
import nightcrawler.crawler.webdriver.infiniteScroll
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.FluentWait
import java.util.concurrent.TimeUnit

/**
 * Created by andrea on 15/02/17.
 */
open class FIND : WebDriverCommand() {

    override fun execute(): Any? {
        var by: By? = params["by"] as By?
        var timeout : Int = params["timeout"] as Int? ?: 40
        var pollingEvery : Int = params["pollingEvery"] as Int? ?: 5

        if (by == null)
            by = if (params["cssSelector"] != null) By.cssSelector(params["cssSelector"] as String) else null

        return true
    }


}


fun WebDriver.findElementsWithFuentWait(by: By, timeoutSeconds: Int = 40, pollingEverySeconds: Int = 5): List<WebElement> {
    val wait = FluentWait<WebDriver>(this)
            .withTimeout(20, TimeUnit.SECONDS)
            .pollingEvery(5, TimeUnit.SECONDS)
            .ignoring(NoSuchElementException::class.java)

    val function = Function<WebDriver, List<WebElement>> {
        this.findElements(by)
    }
    return wait.until(function)

}