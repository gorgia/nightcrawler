package nightcrawler.crawler.webdriver

import nightcrawler.utils.log
import org.openqa.selenium.WebDriver

/**
 * Created by andrea on 08/08/16.
 */
class WebDriverFreeShutdownHook : Runnable {
    override fun run() {
        log().warn("Successfully called ${this.javaClass}")
    //    WebDriverFactory.webDriverStorage.forEach(WebDriver::quit)
    }
}