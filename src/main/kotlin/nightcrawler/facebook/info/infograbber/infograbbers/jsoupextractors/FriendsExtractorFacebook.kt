package nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors

import nightcrawler.facebook.info.extractFacebookIdFromElement
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by andrea on 04/08/16.
 */

@Component
class FriendsExtractorFacebook : JsoupDataExtractor {

    override fun extractData(element: Element): LinkedHashMap<String, Element> {
        val friends = LinkedHashMap<String, Element>()
        if (element.getElementsMatchingOwnText("Tutti gli amici").isEmpty() && element.getElementsMatchingOwnText("All Friends").isEmpty()) {
            return friends
        }
        val badgesContainer = element.getElementById("collection_wrapper_2356318349")
        val badges = badgesContainer.select("[href ^= https://www.facebook.com/], [data-hovercard ^= /ajax/hovercard]")!!//subElement.getElementsByClass("_698")
        badges.forEach({ badge ->
            val facebookId = extractFacebookIdFromElement(badge)
            if (facebookId != null) friends.put(facebookId, badge)
        })
        return friends
    }
}