package nightcrawler.crawler.utils

import nightcrawler.crawler.Crawler
import nightcrawler.crawler.webdriver.getMachineIp
import nightcrawler.crawler.webdriver.takeScreenshot
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.JsoupDataExtractor
import nightcrawler.utils.log
import org.jsoup.nodes.Element
import org.openqa.selenium.remote.RemoteWebDriver
import java.util.*
import java.util.regex.Pattern

/**
 * Created by andrea on 27/07/16.
 */
object ExternalIpExtractor : JsoupDataExtractor {

    val mapMachineExternalIp = HashMap<String, String>()


    fun getExternalIp(crawler: Crawler): String {
        val webDriver = crawler.webDriver
        var machineIp: String? = null
        var externalip: String? = null
        if (webDriver is RemoteWebDriver) {
            machineIp = webDriver.getMachineIp()
            externalip = mapMachineExternalIp[machineIp]
            if (externalip != null) return externalip
        }
        var retryCount = 0
        do {
            try {
                webDriver.get("https://api.ipify.org") //exception to the usage of crawler, it should not be necessary
                val pageSource = webDriver.pageSource
                externalip = extractIpFromText(pageSource!!)
                if (machineIp != null) mapMachineExternalIp.put(machineIp, externalip)
            } catch(e: Exception) {
                log().error("Exception during extraction of ip", e)
                crawler.webDriver.takeScreenshot("ip-estraction-error.jpg")
            }
        } while (externalip == null && ++retryCount < 2)

        return externalip ?: "0.0.0.0"
    }


    override fun extractData(element: Element): Any {
        val externalIP: String
        val ipbox = element.getElementsByClass("ip").first()
        externalIP = extractIpFromText(ipbox.text())
        log().info("External ip of webDriver is " + externalIP)
        return externalIP
    }

    private fun extractIpFromText(text: String): String {
        val IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
        val pattern = Pattern.compile(IPADDRESS_PATTERN)
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            return matcher.group(0)
        } else {
            return "0.0.0.0"
        }
    }

}