package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.database.mongodb.models.FacebookTargetMongo
import nightcrawler.database.neo4j.models.*
import nightcrawler.facebook.info.FacebookNeo4jEntityFactory
import nightcrawler.facebook.info.bancheck.FacebookBanCheckerFacebook
import nightcrawler.facebook.info.baseUrlFromFacebookName
import nightcrawler.facebook.info.extractFacebookNameFromHref
import nightcrawler.facebook.info.extractIdFromHovercardAttribute
import nightcrawler.facebook.info.infograbber.exceptions.FacebookAccountDoesNotExistsException
import nightcrawler.facebook.targetmanager.MongoTargetManager
import nightcrawler.springconfiguration.ApplicationContextProvider
import nightcrawler.utils.StringMatcher
import nightcrawler.utils.log
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

/**
 * Created by andrea on 05/08/16.
 */
@Component
@Scope("prototype")
class BasicInfoGrabber(val ft: FacebookTargetMongo) : AtomicFacebookPageReacher(), InitializingBean {


    @Autowired
    lateinit var facebookNeo4jEntityFactory: FacebookNeo4jEntityFactory

    lateinit var facebookBanChecker: FacebookBanCheckerFacebook

    @Autowired
    private lateinit var facebookTargetManagerMongo: MongoTargetManager

    override fun afterPropertiesSet() {
        facebookBanChecker = ApplicationContextProvider.ctx!!.getBean(FacebookBanCheckerFacebook::class.java)
    }

    fun process(): FacebookPage {
        val url = baseUrlFromFacebookName(ft.facebookIdentifier!!)
        var facebookPage: FacebookPage
        log().debug("BasicInfoGrabbing at page account $url")
        val pageSource = getPage(url)!! //executeCrawlerCommands()!!
        if (!doesPageExists(pageSource)) {
            forgetAccountsForWeeks(ft, 2)
            facebookPage = facebookNeo4jEntityFactory.getOrCreateFacebookPageFromTarget(ft, FacebookPage::class.java)
            facebookPage.hydrationDate = Date()
            facebookPage.doesExist = false
            facebookPage.facebookId = ft.facebookIdentifier
            facebookNeo4jEntityFactory.saveFacebookAccount(facebookPage, 1)
            return facebookPage
        }
        val doc = Jsoup.parse(pageSource)
        val facebookId = getFacebookId(doc)!!
        ft.doesExist = true
        val pageType = getPageType(doc)
        facebookPage = facebookNeo4jEntityFactory.getByFacebookId(facebookId, pageType) ?: getOrCreateCorrectFacebookPageType(doc, ft)
        if (facebookPage.javaClass != pageType) {
            facebookPage = facebookNeo4jEntityFactory.changeFacebookPageType(facebookPage, pageType)
        }
        facebookPage.facebookId = facebookId
        facebookPage.doesExist = true
        facebookPage.facebookName = getFacebookName(doc) ?: facebookPage.facebookName
        facebookPage.name = getName(doc) ?: facebookPage.name
        facebookPage.coverPhotoImg = getCoverImg(doc) ?: facebookPage.coverPhotoImg
        facebookPage.profilePic = getProfilePic(doc) ?: facebookPage.profilePic
        if (ft.neo4jNodeId == null) ft.neo4jNodeId = facebookPage.id
        return facebookPage
    }

    private fun forgetAccountsForWeeks(ft: FacebookTargetMongo, forgetWeeks: Long = 2) {
        if (!ft.saveInDb) return
        log().warn("${ft.facebookIdentifier} does not exists: Please check. I will forget about it for $forgetWeeks weeks")
        ft.lastUpdateAttributes = LocalDateTime.now().plusWeeks(forgetWeeks)
        ft.lastUpdateTimeline = LocalDateTime.now().plusWeeks(forgetWeeks)
        ft.priority = 0
        facebookTargetManagerMongo.updateTarget(ft)
    }

    fun doesPageExists(pageSource: String): Boolean {
        if (StringMatcher.containsAny(pageSource, "pagenotavailable") || StringMatcher.containsAny(pageSource, "Sorry, this content isn't available right now"))
            return false
        return true
    }

    fun getFacebookId(doc: Element): String? {
        var facebookId: String? = null
        try {
            val meta = doc.getElementsByAttributeValueStarting("content", "fb://")
            if (meta.isNotEmpty()) {
                val content = meta.first().attr("content")
                var splittedContent = content.split("/").last()
                splittedContent = if (splittedContent.contains("?")) splittedContent.split("?").first() else splittedContent
                facebookId = splittedContent.toLong().toString() //should raise an exception if facebookId is not numeric
            }
            var noscript: String? = null
            if (facebookId == null) {
                noscript = doc.getElementsByTag("noscript").first()?.html()
                if (noscript != null)
                    facebookId = extractIdFromHovercardAttribute(noscript)?.toLong().toString()
            }
            if (facebookId == null) {
                val entitySidebar = doc.getElementById("entity_sidebar")
                if (entitySidebar != null) {
                    val hovercard = entitySidebar.getElementsByAttributeValueContaining("data-hovercard", "php?id=").firstOrNull()?.attr("data-hovercard")
                    if (!hovercard.isNullOrBlank()) facebookId = extractIdFromHovercardAttribute(hovercard!!)?.toLong().toString()
                }
            }
        } catch (e: Exception) {

        }
        if (facebookId == null) log().error("Impossible to extract facebookId from pageSource:\n ${doc.html()}")
        return facebookId
    }

    fun getFacebookName(doc: Element): String? {
        val noscriptText = doc.getElementsByTag("noscript").first().text()
        if (noscriptText.contains("URL=/")) {
            val url = noscriptText.split("URL=/")[1]
            return extractFacebookNameFromHref(url)
        }
        return null
    }

    fun getProfilePic(doc: Element): String? {
        val profilePicEl = doc.getElementsByClass("profilePic")
        return profilePicEl.first()?.attr("src")
    }

    fun getCoverImg(doc: Element): String? {
        val coverImgEl = doc.getElementsByClass("coverPhotoImg")
        return coverImgEl.attr("src")
    }

    fun getName(doc: Element): String? {
        val pageTitle: String? = doc.getElementById("pageTitle")?.text()?.trim() ?: return null
        if (pageTitle!!.startsWith("(")) return pageTitle.split(")")[1].trim()
        return pageTitle
    }


    fun getOrCreateCorrectFacebookPageType(doc: Element, ft: FacebookTargetMongo): FacebookPage {
        val pageType = getPageType(doc)
        var fp: FacebookPage? = facebookNeo4jEntityFactory.getOrCreateFacebookPageFromTarget(ft, FacebookPage::class.java)
        if (fp!!.javaClass == pageType) {
            return fp
        } else {
            return facebookNeo4jEntityFactory.changeFacebookPageType(fp, pageType)
        }
    }

    fun getPageType(doc: Element): Class<out FacebookPage> {
        try {
            val noscriptText = doc.getElementsByTag("noscript").first().text()
            if (noscriptText.contains("groups")) return FacebookGroup::class.java
            val pageTitleText = doc.getElementById("pageTitle").text()
            if (pageTitleText.contains("Home")) return FacebookBusinessPage::class.java
            if (noscriptText.contains("pages") || noscriptText.contains("places")) return FacebookPage::class.java
            val fbElement = doc.getElementsByAttributeValueStarting("content", "fb://")
            if (fbElement.first().attr("content").contains("group")) return FacebookGroup::class.java
            if (fbElement.first().attr("content").contains("page")) return FacebookPage::class.java
            if (fbElement.first().attr("content").contains("profile")) return FacebookAccount::class.java
        } catch (e: Exception) {
            log().error("Expected Element not found.", e)
            //crawler.webDriver.takeScreenshot("error-screenshots/${sanitizeUrltoPath(crawler.webDriver.currentUrl)}-elementNotFoundErr.jpg")
        }
        return FacebookAccount::class.java
    }


}