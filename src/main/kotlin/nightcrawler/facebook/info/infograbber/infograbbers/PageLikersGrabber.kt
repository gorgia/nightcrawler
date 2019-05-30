package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.database.neo4j.models.FacebookBusinessPage
import nightcrawler.database.neo4j.models.FacebookEntity
import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.JsoupDataExtractor
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.SearchExtractorFacebook
import nightcrawler.utils.log
import org.jsoup.nodes.Element
import org.openqa.selenium.By
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by andrea on 05/08/16.
 */
@Component
@Scope("prototype")
class PageLikersGrabber(override val facebookPage: FacebookBusinessPage) : InfoGrabberCommandBase(scrollBy = By.className("_1yt")) {



    override fun createUrl(): String = "https://www.facebook.com/search/${facebookPage.facebookId}/likers"

    override fun updateFacebookAccount(facebookEntitiesMap: LinkedHashMap<String, Element>) {
        log().debug("Grabbed ${facebookEntitiesMap.keys.size} different LIKERS for businesspage ${facebookPage.facebookName}")
        val oldLikesFacebookId: List<String?> = facebookPage.likers.map(FacebookEntity::facebookId)
        facebookEntitiesMap.filterKeys { key -> !oldLikesFacebookId.contains(key) }
        facebookPage.likers.addAll(facebookNeo4jEntityFactory.getOrCreateFacebookPageFromElements(FacebookPage::class.java, facebookEntitiesMap.values))
        log().debug("fp: ${facebookPage.facebookName ?: facebookPage.facebookId} oldlikers: ${oldLikesFacebookId.size} new likers: ${facebookEntitiesMap.values.size} total: ${(facebookPage).likes.size}")
    }

    override var dataExtractor: JsoupDataExtractor = SearchExtractorFacebook()

}