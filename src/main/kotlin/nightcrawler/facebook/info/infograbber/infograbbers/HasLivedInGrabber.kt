package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.crawler.Crawler
import nightcrawler.database.neo4j.models.FacebookAccount
import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.facebook.info.baseUrlFromFacebookAccount
import nightcrawler.facebook.info.getFirstRestSeparator
import nightcrawler.facebook.info.getSecondRestSeparator
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.JsoupDataExtractor
import nightcrawler.utils.log
import org.jsoup.nodes.Element
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by andrea on 05/08/16.
 */
@Component
@Scope("prototype")
open class HasLivedInGrabber(override val facebookPage: FacebookAccount) : InfoGrabberCommandBase() {
    override fun createUrl(): String {
        val baseUrl = baseUrlFromFacebookAccount(facebookPage)
        return "$baseUrl${getFirstRestSeparator(facebookPage.facebookName?:facebookPage.facebookId)}about${getSecondRestSeparator(facebookPage.facebookName?:facebookPage.facebookId)}section=living"
    }

    override fun updateFacebookAccount(facebookEntitiesMap: LinkedHashMap<String, Element>) {
        log().debug("Grabbing HAS_LIVED_IN for account: ${facebookPage.facebookName}")
        if (facebookEntitiesMap.isEmpty()) return
        if (facebookPage.livesin?.facebookId?.equals(facebookEntitiesMap.entries.first().key) ?: false)
            facebookPage.livesin = facebookNeo4jEntityFactory.getOrCreateFacebookPageFromElement(facebookEntitiesMap.entries.first().value)
        facebookEntitiesMap.remove(facebookEntitiesMap.entries.first().key)
        facebookPage.hasLivedIn.addAll(facebookNeo4jEntityFactory.getOrCreateFacebookPageFromElements(FacebookPage::class.java, facebookEntitiesMap.values))
    }

    @Autowired
    @Qualifier("hasLivedInExtractorFacebook")
    override lateinit var dataExtractor: JsoupDataExtractor

}