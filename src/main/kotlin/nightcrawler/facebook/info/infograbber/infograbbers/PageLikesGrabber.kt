package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.database.neo4j.models.FacebookAccount
import nightcrawler.database.neo4j.models.FacebookBusinessPage
import nightcrawler.database.neo4j.models.FacebookEntity
import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.JsoupDataExtractor
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.LikesExtractorFacebook
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.PageLikesExtractorFacebook
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
class PageLikesGrabber(override val facebookPage: FacebookBusinessPage) : InfoGrabberCommandBase(scrollBy = By.className("uiList")) {

    override fun createUrl(): String = "https://www.facebook.com/browse/fanned_pages/?id=${facebookPage.facebookId}"

    override fun updateFacebookAccount(facebookEntitiesMap: LinkedHashMap<String, Element>) {
        log().debug("Grabbed ${facebookEntitiesMap.keys.size} different LIKES for businesspage ${facebookPage.facebookName}")
        val oldLikesFacebookId: List<String?> = facebookPage.likes.map(FacebookEntity::facebookId)
        facebookEntitiesMap.filterKeys { key -> !oldLikesFacebookId.contains(key) }
        facebookPage.likes.addAll(facebookNeo4jEntityFactory.getOrCreateFacebookPageFromElements(FacebookPage::class.java, facebookEntitiesMap.values))
        log().debug("fp: ${facebookPage.facebookName ?: facebookPage.facebookId} oldlikes: ${oldLikesFacebookId.size} new likes: ${facebookEntitiesMap.values.size} total: ${(facebookPage).likes.size}")
    }

    override var dataExtractor: JsoupDataExtractor = PageLikesExtractorFacebook()

}