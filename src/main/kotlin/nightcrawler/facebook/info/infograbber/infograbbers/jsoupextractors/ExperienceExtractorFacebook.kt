package nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors

import nightcrawler.crawler.exceptions.PageNotReachableException
import nightcrawler.facebook.info.extractFacebookIdFromElement
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by andrea on 26/07/16.
 */
@Component
class ExperienceExtractorFacebook : JsoupDataExtractor {

    override fun extractData(element: Element): LinkedHashMap<String, Element> {
        val experiences = LinkedHashMap<String, Element>()
        val eduworkPagelet = element.getElementById("pagelet_eduwork") ?: throw PageNotReachableException("Work and Education page")
        val experiencesElements = eduworkPagelet.getElementsByClass("fbEditProfileViewExperience")
        experiencesElements.forEach {
            val wdatagts = it.getElementsByAttributeValueStarting("data-hovercard", "/ajax/hovercard")
            if (wdatagts.isEmpty()) return experiences
            val wdatagt = wdatagts[0]
            val facebookId = extractFacebookIdFromElement(wdatagt)
            if (facebookId != null) experiences[facebookId] = wdatagt
        }
        return experiences
    }

}