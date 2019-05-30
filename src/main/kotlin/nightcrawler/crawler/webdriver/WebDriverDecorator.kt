package nightcrawler.crawler.webdriver

import com.assertthat.selenium_shutterbug.utils.file.FileUtil
import com.assertthat.selenium_shutterbug.utils.image.ImageProcessor
import com.assertthat.selenium_shutterbug.utils.web.Coordinates
import com.google.common.base.Function
import com.google.common.util.concurrent.SimpleTimeLimiter
import com.google.common.util.concurrent.TimeLimiter
import nightcrawler.facebook.info.sanitizeUrltoPath
import nightcrawler.utils.conf
import nightcrawler.utils.log
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHttpEntityEnclosingRequest
import org.apache.http.util.EntityUtils
import org.json.JSONObject
import org.openqa.selenium.*
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.remote.SessionId
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait
import org.openqa.selenium.support.ui.Wait
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.imageio.ImageIO
import kotlin.concurrent.timerTask


/**
 * Created by andrea on 05/04/16.
 */

fun WebDriver.scrollToBottomOnce() {
    val jse: JavascriptExecutor = this as JavascriptExecutor
    this.log().debug("${this} : Scrolling...")
    jse.executeScript("window.scrollTo(0, document.body.scrollHeight)")
}

fun WebDriver.infiniteScroll(heartBitSeconds: Int = 5, maxScrollCount: Int = 1000, maxTimeInMinutes: Int = 10, by: By? = null) {
    val singleThreadExecutor = Executors.newSingleThreadExecutor()
    var scrollCount = 0
    var elementsCount = 0
    var iterationCount = 0
    val wait: Wait<WebDriver> = FluentWait<WebDriver>(this)
            .withTimeout(heartBitSeconds.toLong() * 10, TimeUnit.SECONDS)
            .pollingEvery(heartBitSeconds.toLong(), TimeUnit.SECONDS)
            .ignoring(NoSuchElementException::class.java)
    val future = singleThreadExecutor.submit({
        var stop = false
        while (!stop && scrollCount < maxScrollCount) {
            try {
                scrollToBottomOnce()
                scrollCount++
                wait.until({
                    iterationCount = getNumberOfChoosenElements(by)
                    iterationCount > elementsCount
                })
                if (elementsCount == iterationCount) stop = true
                else elementsCount = iterationCount
            } catch (e: Exception) { //this exception comes from wait.until and is therefore a necessary catch
                stop = true
            }
        }
    })

    try {
        future.get(maxTimeInMinutes.toLong(), TimeUnit.MINUTES)
    } catch (e: Exception) {
        if (e is InterruptedException || e is TimeoutException) {
            log().debug("Infinite Scroll incurred in Timeout Exception at url: ${this.currentUrl}", e)
        } else log().error("Unattended exception during Infinite Scroll", e)
    } finally {
        future.cancel(true)
    }
}

/*
fun WebDriver.infiniteScroll(heartBitSeconds: Int = 40, maxScrollCount: Int = 50, maxTimeInMinutes: Int = 10, by: By? = null): Map<String, Int> {
    var elementsCount = 0
    var newElementsCount = 0
    var scrollCount = 0
    val map = HashMap<String, Int>()
    var currentUrl: String? = null
    try {
        val maxTimeStart = System.currentTimeMillis()
        var maxTimeEnd = System.currentTimeMillis()
        var duration = maxTimeEnd - maxTimeStart
        currentUrl = this.currentUrl
        while (scrollCount < maxScrollCount && duration < maxTimeInMinutes * 1000 * 60) {
            scrollToBottomOnce(heartBitSeconds)
            scrollCount++
            var endTime: Long = 0
            val startTime = System.currentTimeMillis()
            do {
                scrollToBottomOnce(heartBitSeconds)
                scrollCount++
                log().debug("Scrolling count: $scrollCount")
                val iterationElementsCount = getNumberOfChoosenElements(by)
                if (iterationElementsCount > newElementsCount) {
                    newElementsCount = iterationElementsCount
                    continue
                }
                endTime = System.currentTimeMillis()
            } while (endTime - startTime < heartBitSeconds * 1000)
            if (elementsCount == newElementsCount) {
                break
            }
            elementsCount = newElementsCount
            maxTimeEnd = System.currentTimeMillis()
            duration = maxTimeEnd - maxTimeStart
        }
        map.put("elementscount", elementsCount)
    } catch (e: Exception) {
        log().error("InfiniteScroll exception", e)
        this.takeScreenshot("error-screenshots/scroll/${nightcrawler.facebook.info.sanitizeUrltoPath(currentUrl!!)}.jpg")
    }
    return map
}*/


fun WebDriver.getNumberOfChoosenElements(by: By? = null): Int {
    return if (by == null)
        this.findElements(org.openqa.selenium.By.xpath("//*")).size
    else
        this.findElements(by).size
}

fun WebDriver.takeScreenshot(filepath: String? = null, executingCommand: String? = null, fullpage: Boolean = false, vararg elementsToBlur: WebElement): Any? {
    try {
        val pathFile: String
        if (executingCommand != null)
            pathFile = filepath ?: "error-screenshots/pagereacher/ ${sanitizeUrltoPath(this.currentUrl)}-${executingCommand.javaClass}.jpg"
        else {
            pathFile = filepath ?: "error-screenshots/pagereacher/ ${sanitizeUrltoPath(this.currentUrl)}.jpg"
        }
        if (fullpage) {
            val jse: JavascriptExecutor = this as JavascriptExecutor
            val width = jse.executeScript("return Math.max(document.body.scrollWidth, document.body.offsetWidth, document.documentElement.clientWidth, document.documentElement.scrollWidth, document.documentElement.offsetWidth);") as Long
            val height = jse.executeScript("return Math.max(document.body.scrollHeight, document.body.offsetHeight, document.documentElement.clientHeight, document.documentElement.scrollHeight, document.documentElement.offsetHeight);") as Long
            this.manage().window().size = Dimension(width.toInt(), height.toInt())
            Thread.sleep(1000)
        }
        val screenShotTaker = this as TakesScreenshot
        val screnshotFile: File = screenShotTaker.getScreenshotAs(OutputType.FILE)
        var fullImg = ImageIO.read(screnshotFile)
        elementsToBlur.forEach { fullImg = ImageProcessor.blurArea(fullImg, Coordinates(it)); }
        val destFile = File(pathFile)
        if (!Files.exists(Paths.get(pathFile))) {
            destFile.mkdirs()
        }
        FileUtil.writeImage(fullImg, "png", destFile)
        log().info("Screenshot taken at: $filepath")
        return screnshotFile
    } catch (e: Exception) {
        log().error("Impossible to take screenshot", e)
    }
    return null
}

fun RemoteWebDriver.getMachineIp(): String {
    val yourHubIP = conf.getString("socialnet.webDriver.seleniumHubGridServer")
    val yourHubPort = conf.getInt("socialnet.webDriver.seleniumHubGridPort")
    val host = HttpHost(yourHubIP, yourHubPort)
    val client: CloseableHttpClient = HttpClientBuilder.create().build()
    val testSessionApi = URL("http://" + yourHubIP + ":" + yourHubPort + "/grid/api/testsession?session=" + this.sessionId)
    val r = BasicHttpEntityEnclosingRequest("GET", testSessionApi.toExternalForm())
    val response: HttpResponse = client.execute(host, r)
    val jsonObject = JSONObject(EntityUtils.toString(response.getEntity()))
    val proxyID = jsonObject.get("proxyId")
    val proxyIDString = proxyID.toString()
    return proxyIDString.split("//")[1].split(":")[0]
}


fun WebDriver.getCookies(): HashSet<Cookie> {
    return this.manage().cookies.mapTo(HashSet<Cookie>(), ::Cookie)
}

fun WebDriver.findElementsWithFuentWait(by: By, timeoutSeconds: Long = 40, pollingEverySeconds: Long = 5): List<WebElement> {
    synchronized(this) {
        val function = Function<WebDriver, List<WebElement>> {
            this.findElements(by)
        }

        val wait: Wait<WebDriver> = FluentWait<WebDriver>(this)
                .withTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .pollingEvery(pollingEverySeconds, TimeUnit.SECONDS)
                .ignoring(NoSuchElementException::class.java)

        return wait.until(function)
    }

}


