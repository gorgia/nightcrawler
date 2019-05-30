package nightcrawler.facebook2.jsoupextractors

import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.JsoupDataExtractor
import nightcrawler.utils.log
import org.jsoup.Jsoup

/**
 * Created by andrea on 25/11/16.
 */
interface ExtractionTask {
    var pageSource: String
    var extractor: JsoupDataExtractor

    fun execute(): Any? {
        var result: Any? = null
        try {
            val document = Jsoup.parse(pageSource)
            result = extractor.extractData(document)
        } catch(e: Exception) {
            log().error("Error during extractionTask", e)
        }
        return result
    }
}