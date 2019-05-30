package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.crawler.Crawler
import nightcrawler.database.neo4j.models.FacebookAccount
import nightcrawler.database.neo4j.models.FacebookEntity
import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.facebook.info.baseUrlFromFacebookAccount
import nightcrawler.facebook.info.getFirstRestSeparator
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.FriendsExtractorFacebook
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
class FriendsGrabber(override val facebookPage: FacebookPage) : InfoGrabberCommandBase(scrollBy = By.className("uiList")) {

    override fun createUrl(): String =//return "https://www.facebook.com/profile.php?id=${facebookPage.facebookId}&sk=friends&collection_token=${facebookPage.facebookId}%3A2356318349%3A2"
            "https://www.facebook.com/${facebookPage.facebookId}/friends"

    override fun updateFacebookAccount(facebookEntitiesMap: LinkedHashMap<String, Element>) {
        log().debug("Grabbed ${facebookEntitiesMap.keys.size} different FRIENDS of ${facebookPage.facebookName}")
        if (facebookPage is FacebookAccount) {
            val oldFriendsFacebookIds: List<String?> = (facebookPage as FacebookAccount).friends.map({ friendEntity: FacebookEntity -> friendEntity.facebookId })
            facebookEntitiesMap.filterKeys { key -> !oldFriendsFacebookIds.contains(key) }
            (facebookPage as FacebookAccount).friends.addAll(facebookNeo4jEntityFactory.getOrCreateFacebookPageFromElements(FacebookAccount::class.java, facebookEntitiesMap.values))
            log().debug("fp: ${facebookPage.facebookName?:facebookPage.facebookId} oldfriends: ${oldFriendsFacebookIds.size} new friends: ${facebookEntitiesMap.values.size} total: ${(facebookPage as FacebookAccount).friends.size}")
        }
    }

    override var dataExtractor: JsoupDataExtractor = FriendsExtractorFacebook()

}