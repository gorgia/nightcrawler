package nightcrawler.crawler.commands

import org.openqa.selenium.WebDriver

/**
 * Created by andrea on 30/11/16.
 */

open class WebDriverInvokerDefault(override var webDriver: WebDriver) : WebDriverInvoker {


    override fun invoke(closeAfter: Boolean, vararg commands: WebDriverCommand): String? {
        synchronized(this.webDriver) {
            commands.forEach { command ->
                if (command.webDriver == null) command.webDriver = this.webDriver
                val commandResult = command.execute()
                if (commandResult is Boolean)
                    if (!commandResult)
                        throw Exception("Some exception occurred during execution of command: $command")
            }
            return this.webDriver.pageSource
        }
    }
}