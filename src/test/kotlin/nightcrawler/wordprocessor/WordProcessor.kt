package nightcrawler.wordprocessor

import com.google.common.io.Resources
import nightcrawler.utils.log
import org.apache.tika.exception.TikaException
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import java.util.regex.Pattern


/**
 * Created by andrea on 23/01/17.
 */
class WordProcessor {


    val directory = ""
    val filename = "tikaTest"
    val testString = "https://www.facebook.com/regextest"

    @Test
    fun fileTreeWalk() {
        val url = Resources.getResource(filename)
        val file = File(url.file)
        if (file.isDirectory) {
            file.walkTopDown().filter { it.isFile }.forEach{ parseFile(it) }
        }
    }

    fun parseFile(file: File) {
        val stream = file.inputStream()
        val parser = AutoDetectParser()
        val handler = BodyContentHandler()
        val parseContext = ParseContext()
        val metadata = org.apache.tika.metadata.Metadata()
        stream.use({ it ->
            parser.parse(it, handler, metadata, parseContext)
            log().info(handler.toString())
            assertNotNull(handler.toString())
        })
    }

    @Test
    fun extractFacebookStringFromText(){
        val pattern : Pattern = Pattern.compile("^.*facebook.*$")
        val matcher = pattern.matcher(testString)
        log().info(matcher.group(0))
    }


    @Throws(IOException::class, TikaException::class)
    fun parseExample() {
        val parser = AutoDetectParser()
        val handler = BodyContentHandler()
        val metadata = org.apache.tika.metadata.Metadata()
        val url = Resources.getResource(filename)
        val stream = url.openStream()
        stream.use({ it ->
            parser.parse(it, handler, metadata)
            log().info(handler.toString())
            assertNotNull(handler.toString())
        })
    }
}