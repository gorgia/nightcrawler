package nightcrawler.crawler.commands

/**
 * Created by andrea on 13/12/16.
 */



object CommandFactory {

    fun <T : WebDriverCommand> createCommand(commandClass: Class<T>, params: Map<String, Any?>): T {
        val command = commandClass.getDeclaredConstructor().newInstance()
        command.params = params
        return command
    }
}