package nightcrawler.crawler

import nightcrawler.crawler.commands.WebDriverCommand
import nightcrawler.crawler.webdriver.takeScreenshot
import nightcrawler.utils.log
import org.openqa.selenium.WebDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.io.Closeable


@Component
@Scope("prototype")
@Lazy
class Crawler : Closeable {

    @Autowired
    lateinit var webDriver: WebDriver

    fun invoke(closeAfter: Boolean = true, vararg commands: WebDriverCommand): String? {
        var executingCommand: WebDriverCommand? = null
        synchronized(this.webDriver) {
            try {
                commands.forEach { command ->
                    executingCommand = command
                    if (command.webDriver == null) command.webDriver = this.webDriver
                    val commandResult = command.execute()
                    if (commandResult is Boolean) {
                        if (!commandResult) {
                            throw Exception("Some exception occurred during execution of command:${command::class.java}")
                        }
                    }
                }
                return this.webDriver.pageSource
            } catch(e: Exception) {
                log().error("Exception during execution of Browser Command ${executingCommand?.let { it::class.java }}", e)
                this.webDriver.takeScreenshot(executingCommand = executingCommand?.let { it::class.java }.toString())
            } finally {
                if (closeAfter) this.close()
            }
        }
        return null
    }

    override fun close() {
        log().debug("Closing webDriver: $webDriver")
        try {
            synchronized(webDriver) {
                this.webDriver.quit()
            }
        } catch(e: Exception) {
            log().error("Exception during close of webDriver!", e)
        }
    }
}