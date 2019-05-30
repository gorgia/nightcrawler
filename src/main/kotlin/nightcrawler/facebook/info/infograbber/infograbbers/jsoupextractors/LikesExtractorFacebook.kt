package nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors

import nightcrawler.facebook.info.extractFacebookIdFromElement
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component


/**
 * Created by andrea on 04/08/16.
 */

@Component
class LikesExtractorFacebook : JsoupDataExtractor {

    override fun extractData(element: Element): LinkedHashMap<String, Element> {
        val likedEntities: LinkedHashMap<String, Element> = LinkedHashMap()
        if (element.getElementsMatchingOwnText("Tutti i like").isEmpty() && element.getElementsMatchingOwnText("All Likes").isEmpty()) {
            return likedEntities
        }
        val timelineMedleyLikes: Element = element.getElementById("pagelet_timeline_medley_likes") ?: return likedEntities
        val hovercards = timelineMedleyLikes.select("[href ^= https://www.facebook.com/][data-hovercard^=/ajax/hovercard]")
        hovercards?.forEach { hovercard ->
            val facebookId = extractFacebookIdFromElement(hovercard)
            if (facebookId != null) likedEntities[facebookId] = hovercard
        }
        return likedEntities
    }

    //just an example
    fun detectMostFrequentClass(doc: Element): String {
        val likesCollectionElement = doc.getElementById("pagelet_timeline_medley_likes")
        val lielements = likesCollectionElement.getElementsByTag("li").toTypedArray()
        val frequentClassNames: MutableMap<String, Int> = HashMap()
        lielements.forEach { it.classNames().forEach<String> { frequentClassNames.getOrPut(it) { 0 }.inc() } }
        return frequentClassNames.toList().sortedByDescending { (key, value) -> value }.first().first
    }

}