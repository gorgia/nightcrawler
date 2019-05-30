package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.crawler.Crawler
import nightcrawler.database.neo4j.models.FacebookEntity
import nightcrawler.database.neo4j.models.FacebookGroup
import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.facebook.info.baseUrlFromFacebookAccount
import nightcrawler.facebook.info.getFirstRestSeparator
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.GroupMembersExtractorFacebook
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.JsoupDataExtractor
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
open class GroupMembersGrabber(override val facebookPage: FacebookPage) : InfoGrabberCommandBase( scrollBy = By.className("uiList")) {
    override fun updateFacebookAccount(facebookEntitiesMap: LinkedHashMap<String, Element>) {
        log().debug("Grabbing MEMBERS of ${facebookPage.facebookName}")
        val oldFriendsFacebookIds : List<String?> = (facebookPage as FacebookGroup).members.map({ likesEntity : FacebookEntity -> likesEntity.facebookId})
        facebookEntitiesMap.filterKeys { key -> !oldFriendsFacebookIds.contains(key)}
        (facebookPage as FacebookGroup).members.addAll(facebookNeo4jEntityFactory.getOrCreateFacebookPageFromElements(FacebookPage::class.java, facebookEntitiesMap.values))
    }

    override fun createUrl(): String {
        val baseUrl = baseUrlFromFacebookAccount(facebookPage)
        return "$baseUrl${getFirstRestSeparator(facebookPage.facebookName?:facebookPage.facebookId)}members"
    }


    override var dataExtractor: JsoupDataExtractor = GroupMembersExtractorFacebook()

}