package nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors

import nightcrawler.facebook.info.extractFacebookIdFromSearchElement
import nightcrawler.utils.log
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.util.LinkedHashMap


/**
 * Created by andrea on 04/08/16.
 */

@Component
class SearchExtractorFacebook : JsoupDataExtractor {


    override fun extractData(element: Element): LinkedHashMap<String, Element> {
        val likers = LinkedHashMap<String, Element>()
        val resultContainerElement = element.getElementById("initial_browse_result")
        if(resultContainerElement == null)  {
            log().warn("No page elements found!")
            return likers
        }
        val searchItems = resultContainerElement.getElementsByClass("_4p2o")
        searchItems.forEach({ searchItem ->
            val facebookId = extractFacebookIdFromSearchElement(searchItem)
            if (facebookId != null) likers.put(facebookId, searchItem)
        })
        return likers
    }


}