package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.crawler.Crawler
import nightcrawler.database.neo4j.models.FacebookAccount
import nightcrawler.database.neo4j.models.FacebookEntity
import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.facebook.info.baseUrlFromFacebookAccount
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.JsoupDataExtractor
import nightcrawler.utils.log
import org.jsoup.nodes.Element
import org.openqa.selenium.By
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
class FollowingGrabber(override val facebookPage: FacebookAccount) : InfoGrabberCommandBase( scrollBy = By.className("fbProfileBrowserList"), attributeToCheck = "name", containingAttribute =  "following") {
    override fun createUrl(): String =
            "https://www.facebook.com/${facebookPage.facebookId}/following"
            //"https://www.facebook.com/profile.php?id=${facebookPage.facebookId}&sk=friends&collection_token=${facebookPage.facebookId}%3A2356318349%3A33"//"$baseUrl${getFirstRestSeparator(facebookPage.facebookName?:facebookPage.facebookId)}following"

    override fun updateFacebookAccount(facebookEntitiesMap: LinkedHashMap<String, Element>) {
        log().debug("Grabbed ${facebookEntitiesMap.keys.size} different FOLLOWING for account ${facebookPage.facebookName}")
        val oldFollowingFacebookIds : List<String?> = facebookPage.following.map({ likesEntity : FacebookEntity -> likesEntity.facebookId})
        facebookEntitiesMap.filterKeys { key -> !oldFollowingFacebookIds.contains(key)}
        facebookPage.following.addAll(facebookNeo4jEntityFactory.getOrCreateFacebookPageFromElements(FacebookPage::class.java, facebookEntitiesMap.values))
        log().debug("fp: ${facebookPage.facebookName?:facebookPage.facebookId} oldfollowing: ${oldFollowingFacebookIds.size} new followings: ${facebookEntitiesMap.values.size} total: ${(facebookPage).following.size}")
    }

    @Autowired
    @Qualifier("followingExtractorFacebook")
    override lateinit var dataExtractor: JsoupDataExtractor

}