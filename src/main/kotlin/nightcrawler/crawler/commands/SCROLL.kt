package nightcrawler.crawler.commands

import nightcrawler.crawler.webdriver.infiniteScroll
import org.openqa.selenium.By

/**
 * Created by andrea on 02/12/16.
 */
/**
 * @scrollBy looks for elements WebElements of type scrollBy
 * @scrollTimes number of times to scroll default = 50
 */
open class SCROLL : WebDriverCommand() {

    override fun execute(): Any? {
        var by: By? = params["by"] as By?
        var heartBeat: Int = params["heartbeat"] as Int? ?: 60
        if (by == null)
            by = if (params["cssSelector"] != null) By.cssSelector(params["cssSelector"] as String) else null
        val scrollTimes: Int = params["scrollTimes"] as Int? ?: 1000
        val maxTimeInMinutes : Int = params["maxTimeInMinutes"] as Int? ?:10
        synchronized(webDriver!!) {
            this.webDriver!!.infiniteScroll(heartBitSeconds = heartBeat, maxTimeInMinutes = maxTimeInMinutes, by = by, maxScrollCount = scrollTimes)
        }
        return true
    }

}