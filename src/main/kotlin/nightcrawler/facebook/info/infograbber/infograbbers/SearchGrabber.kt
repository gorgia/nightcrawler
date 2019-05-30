package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.facebook.info.FacebookNeo4jEntityFactory
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.JsoupDataExtractor
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.SearchExtractorFacebook
import nightcrawler.utils.log
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.openqa.selenium.By
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*
import kotlin.collections.HashSet

/**
 * Created by andrea on 05/08/16.
 */
@Component
@Scope("prototype")
class SearchGrabber : AtomicFacebookPageReacher() {



    val scrollBy: By? = By.className("_1yt")

    var dataExtractor: JsoupDataExtractor = SearchExtractorFacebook()

    @Autowired
    lateinit var facebookNeo4jEntityFactory: FacebookNeo4jEntityFactory

    fun process(t: String): Set<FacebookPage> {
        try {
            log().debug("Start grabbing data from url: $t")
            val pageSource = getPage(t, scrollBy = scrollBy, heartbeat = 5, maxTimeInMinutes = 20)!!
            val extractorResult: LinkedHashMap<String, Element> = dataExtractor.extractData(Jsoup.parse(pageSource)) as LinkedHashMap<String, Element>
            return facebookNeo4jEntityFactory.getOrCreateFacebookPageFromElementsWithFacebookId(FacebookPage::class.java, extractorResult)
        } catch (e: Exception) {
            log().error("", e)
        }
        return HashSet()
    }

}