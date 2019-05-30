package nightcrawler.crawler.commands

import org.openqa.selenium.WebDriver

/**
 * Created by andrea on 30/11/16.
 */
interface WebDriverInvoker {
    var webDriver: WebDriver
    fun invoke(closeAfter: Boolean = true, vararg commands: WebDriverCommand): Any?
}