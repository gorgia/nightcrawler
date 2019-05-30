package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.database.neo4j.models.FacebookAccount
import nightcrawler.database.neo4j.models.FacebookEntity
import nightcrawler.database.neo4j.models.FacebookPage
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
 class FollowersGrabber(override val facebookPage: FacebookAccount) : InfoGrabberCommandBase( scrollBy = By.className("fbProfileBrowserList"), attributeToCheck = "name", containingAttribute =  "followers") {

    override fun createUrl(): String {
        return "https://www.facebook.com/${facebookPage.facebookId}/followers"
                //"https://www.facebook.com/profile.php?id=${facebookPage.facebookId}&sk=friends&collection_token=${facebookPage.facebookId}%3A2356318349%3A32"
    }

    override fun updateFacebookAccount(facebookEntitiesMap: LinkedHashMap<String, Element>) {
        log().debug("Grabbed ${facebookEntitiesMap.keys.size} different FOLLOWERS for account ${facebookPage.facebookName}")
        val oldFollowersFacebookIds : List<String?> = facebookPage.followers.map(FacebookEntity::facebookId)
        facebookEntitiesMap.filterKeys { key -> !oldFollowersFacebookIds.contains(key)}
        facebookPage.followers.addAll(facebookNeo4jEntityFactory.getOrCreateFacebookPageFromElements(FacebookPage::class.java, facebookEntitiesMap.values))
        log().debug("fp: ${facebookPage.facebookName?:facebookPage.facebookId} oldfollowers: ${oldFollowersFacebookIds.size} new followers: ${facebookEntitiesMap.values.size} total: ${facebookPage.followers.size}")
    }


    @Autowired
    @Qualifier("followersExtractorFacebook")
    override lateinit var dataExtractor: JsoupDataExtractor

}