package nightcrawler.facebook.info.infograbber.timeline.jsoupextractors

import nightcrawler.facebook.info.extractFacebookIdFromPostWrapper
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.JsoupDataExtractor
import nightcrawler.utils.log
import org.jsoup.nodes.Element
import java.util.*

/**
 * Created by andrea on 09/08/16.
 */
class TimelineJsoupExtractor : JsoupDataExtractor {
    override fun extractData(element: Element): HashMap<String, Element> {
        val jsoupElementsMap = HashMap<String, Element>()
        var jsoupWrappers = element.getElementsByClass("fbUserStory")
        if(jsoupWrappers.isEmpty()) jsoupWrappers = element.getElementsByClass("fbUserPost")
        if(jsoupWrappers.isEmpty()) jsoupWrappers = element.getElementsByClass("userContentWrapper")
        if(jsoupWrappers.isEmpty()) jsoupWrappers = element.getElementsByClass("fbUserContent")
        for (jsoupWrapper in jsoupWrappers) {
            try {
                if (jsoupWrapper == null) continue
                val postId: String = extractFacebookIdFromPostWrapper(jsoupWrapper) ?: continue
                jsoupElementsMap.put(postId, jsoupWrapper)
            } catch(e: Exception) {
                log().error("Error during extraction of timeline", e)
            }
        }
        return jsoupElementsMap
    }
}