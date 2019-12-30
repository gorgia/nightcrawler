package nightcrawler.crawler.commands

import nightcrawler.crawler.exceptions.PageNotReachableException
import nightcrawler.utils.log

/**
 * Created by andrea on 02/12/16.
 */

open class GET : WebDriverCommand() {


    override fun execute(): Any? {
        val url: String? = params["url"] as String?
        val checkUrl: Boolean = params["checkUrl"] as Boolean? ?: false
        if (this.webDriver == null) {
            log().error("WebDriver is null for command")
            return null
        }
        var ok: Boolean
        var retryCount = 0
        do {
            try {
                if (url.isNullOrBlank()) this.webDriver!!.navigate().refresh()
                else {
                    var previousUrl: String? = null
                    if (checkUrl) previousUrl = this.webDriver!!.currentUrl
                    this.webDriver!!.get(url)
                    this.waitForPageToLoad()
                    if (checkUrl && this.webDriver!!.currentUrl == previousUrl) {
                        throw PageNotReachableException(url)
                    }
                }
            } catch(e: Exception) {
                if (e is PageNotReachableException) throw e
                else if (retryCount < 2) {
                    log().warn("GET encountered an error at url: $url. Retry", e)
                } else {
                    log().error("GET has been unable to reach url: $url.", e)
                    throw e
                }
            }
            ok = true
        } while (!ok && retryCount++ < 2)
        return ok
    }

}