package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.crawler.Crawler
import nightcrawler.database.neo4j.models.FacebookAccount
import nightcrawler.database.neo4j.models.FacebookEntity
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
class ExperienceGrabber(override var facebookPage: FacebookAccount) : InfoGrabberCommandBase() {

    override fun createUrl(): String {
        val baseUrl = baseUrlFromFacebookAccount(facebookPage)
        return "$baseUrl${getFirstRestSeparator(facebookPage.facebookName?:facebookPage.facebookId)}about${getSecondRestSeparator(facebookPage.facebookName?:facebookPage.facebookId)}section=education"
    }

    override fun updateFacebookAccount(facebookEntitiesMap: LinkedHashMap<String, Element>) {
        log().debug("Grabbing experience for account ${facebookPage.facebookName}")
        val oldExperiencesFacebookIds : List<String?> = facebookPage.experience.map(FacebookEntity::facebookId)
        facebookEntitiesMap.filterKeys { key -> !oldExperiencesFacebookIds.contains(key)}
        facebookPage.experience.addAll(facebookNeo4jEntityFactory.getOrCreateFacebookPageFromElements(FacebookPage::class.java, facebookEntitiesMap.values))
    }

    @Autowired
    @Qualifier("experienceExtractorFacebook")
    override lateinit var dataExtractor: JsoupDataExtractor

}