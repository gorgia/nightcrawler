package nightcrawler.facebook.info.infograbber.timeline.jsoupextractors

import nightcrawler.facebook.info.extractFacebookIdFromElement
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.JsoupDataExtractor
import nightcrawler.utils.log
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by andrea on 04/08/16.
 */
@Component
class PostLikesExtractor : JsoupDataExtractor {

    override fun extractData(element: Element): HashMap<String, Element> {
        val postLikes: HashMap<String, Element> = HashMap()
        try {
            val likesTab = element.select("[id^=reaction_profile_browser]")
            likesTab.forEach {
                val likesli = it.getElementsByAttributeValueStarting("data-hovercard", "/ajax/hovercard/user.php?id=")
                likesli.forEach {
                    val facebookId = extractFacebookIdFromElement(it)
                    if (facebookId != null) postLikes.put(facebookId, it)
                }
            }
        } catch(e: Exception) {
            log().error("", e)
        }
        return postLikes
    }
}