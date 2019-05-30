package nightcrawler.crawler.webdriver

import com.assertthat.selenium_shutterbug.utils.file.FileUtil
import com.assertthat.selenium_shutterbug.utils.image.ImageProcessor
import com.assertthat.selenium_shutterbug.utils.web.Coordinates
import nightcrawler.crawler.Crawler
import nightcrawler.facebook.info.sanitizeUrltoPath
import nightcrawler.utils.log
import org.openqa.selenium.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO

/**
 * Created by andrea on 03/01/17.
 */
object ScreenshotTaker {

    fun takeScreenshot(crawler: Crawler, filepath: String? = null, executingCommand: String? = null, fullpage: Boolean = false, vararg elementsToBlur: WebElement): Any? {
        try {
            val pathFile: String
            if (executingCommand != null)
                pathFile = filepath ?: "error-screenshots/pagereacher/ ${sanitizeUrltoPath(crawler.webDriver.currentUrl)}-${executingCommand.javaClass}.jpg"
            else {
                pathFile = filepath ?: "error-screenshots/pagereacher/ ${sanitizeUrltoPath(crawler.webDriver.currentUrl)}.jpg"
            }
            if (fullpage) {
                val jse: JavascriptExecutor = crawler as JavascriptExecutor
                val width = jse.executeScript("return Math.max(document.body.scrollWidth, document.body.offsetWidth, document.documentElement.clientWidth, document.documentElement.scrollWidth, document.documentElement.offsetWidth);") as Long
                val height = jse.executeScript("return Math.max(document.body.scrollHeight, document.body.offsetHeight, document.documentElement.clientHeight, document.documentElement.scrollHeight, document.documentElement.offsetHeight);") as Long
                crawler.webDriver.manage().window().size = Dimension(width.toInt(), height.toInt())
                Thread.sleep(1000)
            }
            val screenShotTaker = crawler as TakesScreenshot
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
}