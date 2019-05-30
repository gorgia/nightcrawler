package nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors

import nightcrawler.facebook.info.extractFacebookIdFromElement
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by andrea on 04/08/16.
 */

@Component
class FollowingExtractorFacebook : JsoupDataExtractor {

    override fun extractData(element: Element): LinkedHashMap<String, Element> {
        val followings = LinkedHashMap<String, Element>()
        if (element.getElementsMatchingOwnText("Persone seguite").isEmpty() ||  element.getElementsMatchingOwnText("Following").isEmpty()) {
            return followings
        }
        val badgesContainer = element.getElementById("collection_wrapper_2356318349")
        val badges = badgesContainer.getElementsByClass("fbProfileBrowserListItem")!!
        badges.forEach({ badge ->
            val facebookId = extractFacebookIdFromElement(badge)
            if (facebookId != null) followings[facebookId] = badge
        })
        return followings
    }

}