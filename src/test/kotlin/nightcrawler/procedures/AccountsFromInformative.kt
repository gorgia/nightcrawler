package nightcrawler.procedures

import com.google.common.io.Resources
import java.io.File

/**
 * Created by andrea on 23/08/16.
 */
open class AccountsFromInformative {

    var file: File? = null


    open fun getFileContent(filepath: String): String? {
        val url = Resources.getResource(filepath)
        val uri = url.toURI()
        this.file = File(uri)
        val fileContent = file?.readText()
        return fileContent
    }

    open fun getFileContentAsList(filepath: String): MutableList<String>? {
        val url = Resources.getResource(filepath)
        val uri = url.toURI()
        file = File(uri)
        val fileContent = file?.readLines()?.toMutableList()
        return fileContent
    }


    open fun step1(filepath: String) {
        var fileContent = getFileContent(filepath)
        fileContent = fileContent?.replace("http", "\nhttp")
        file?.writeText(fileContent!!)
    }

    open fun step2(filepath: String) {
        var text = getFileContent(filepath)
        var lines = text!!.lines().toMutableList()

        File("citatidallaproduzione.txt").printWriter().use { out ->
            lines.forEach {
                if(it.contains("facebook.com") && !it.contains("group"))
                out.println("$it")
            }
        }




        //file!!.writeText(lines!!.joinToString { "\n" }, Charset.forName("UTF8"))
    }




}


fun main(args: Array<String>) {
    AccountsFromInformative().step2("facebookaccounts.txt")
}