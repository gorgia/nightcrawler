package nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors

import nightcrawler.facebook.info.extractFacebookIdFromElement
import nightcrawler.utils.log
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by andrea on 04/08/16.
 */
@Component
open class HasLivedInExtractorFacebook : JsoupDataExtractor {
    override fun extractData(element: Element): LinkedHashMap<String, Element> {
        val citiesMap: LinkedHashMap<String, Element> = LinkedHashMap()
        try {
            val pagelet_hometown = element.getElementById("pagelet_hometown") ?: return citiesMap
            val cityElements = pagelet_hometown.getElementsByAttribute("data-hovercard")


            cityElements.forEach { cityEl ->
                val facebookId = extractFacebookIdFromElement(cityEl)
                if (facebookId != null) citiesMap.put(facebookId, cityEl)
            }
        } catch (e: Exception) {
            log().error("getLivesIn Exception", e)
        }
        return citiesMap
    }


}