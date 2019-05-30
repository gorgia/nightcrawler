package nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors

import nightcrawler.facebook.info.extractFacebookIdFromElement
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by andrea on 04/08/16.
 */

@Component
class GroupMembersExtractorFacebook : JsoupDataExtractor {


    override fun extractData(element: Element): LinkedHashMap<String, Element> {
        val members = LinkedHashMap<String, Element>()
        if (element.getElementsMatchingOwnText("Membri").isEmpty() || element.getElementsMatchingOwnText("Members").isEmpty()) {
            return members
        }
        val badges = element.getElementsByClass("fbProfileBrowserListItem")
        badges.forEach({ badge ->
            val facebookId = extractFacebookIdFromElement(badge)
            if (facebookId != null) members.put(facebookId, badge)
        })
        return members
    }

}