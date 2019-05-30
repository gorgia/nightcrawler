package nightcrawler.crawler.commands

/**
 * Created by andrea on 01/02/17.
 */
open class SLEEP : WebDriverCommand() {

    override fun execute(): Any? {
        val seconds: Int? = params["seconds"] as Int?
        if (seconds != null) {
            Thread.sleep(seconds * 1000.toLong())
        }
        return true
    }
}