package nightcrawler.facebook.info.bancheck

import nightcrawler.crawler.Crawler
import nightcrawler.database.mongodb.models.FacebookLoginAccount
import nightcrawler.database.mongodb.repositories.FacebookLoginAccountRepository
import nightcrawler.facebook.info.extractFacebookNameFromHref
import nightcrawler.facebook.info.fromUtimeStringToDate
import nightcrawler.facebook.info.infograbber.infograbbers.AtomicFacebookPageReacher
import nightcrawler.facebook.login.FacebookLoginner
import nightcrawler.utils.StringMatcher
import nightcrawler.utils.log
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * Created by andrea on 08/08/16.
 */
@Component
@Scope("prototype")
open class FacebookBanCheckerFacebook : AtomicFacebookPageReacher() {

    var url: String = "https://www.facebook.com/notifications"

    @Autowired
    lateinit var loginner: FacebookLoginner

    @Autowired
    lateinit var mongoLoginAccountRepository: FacebookLoginAccountRepository

    fun checkHasBanProblems(fla: FacebookLoginAccount): Boolean {
        log().info("Check if account ${fla.username} is banned")
        val pageSource = getPage(url)!!

        var debanDate = this.checkDefinitiveBan(pageSource, fla)
        if (debanDate != null) {
            log().warn("FacebookLoginAccount: ${fla.username} is definitively banned!")
            return true
        }
        val facebookName = getFacebookLoginName(Jsoup.parse(pageSource))
        if (fla.facebookName == null) {
            fla.facebookName = facebookName
            mongoLoginAccountRepository.save(fla)
        }
        debanDate = isAccountLimited(pageSource, fla)
        return if (debanDate == null) {
            log().debug("FacebookLoginAccount: ${fla.username} is not banned")
            false
        } else {
            log().debug("FacebookLoginAccount: ${fla.username} is limited until $debanDate")
            true
        }
    }

    private fun isAccountLimited(pageSource: String, fla: FacebookLoginAccount): LocalDateTime? {
        val doc = Jsoup.parse(pageSource)
        val banMessages = doc.getElementsByAttributeValueEnding("href", "notif_t=feature_limits")
        var banDate: Date? = null
        if (banMessages.size == 0) {
            return null
        } else {
            val banMessage = banMessages.first()
            val utimeString = banMessage.getElementsByAttribute("data-utime").first().attr("data-utime")
            if (utimeString != null) {
                banDate = fromUtimeStringToDate(utimeString)
                updateDebanDate(fla, banDate)
            }
        }

        return LocalDateTime.ofInstant(Instant.ofEpochSecond(banDate!!.toInstant().epochSecond), TimeZone
                .getDefault().toZoneId())
    }


    private fun updateDebanDate(fla: FacebookLoginAccount, banDate: Date?) {
        if (banDate != null) {
            val cal = Calendar.getInstance()
            cal.time = banDate
            cal.add(Calendar.DATE, 7)
            val instant = Instant.ofEpochMilli(cal.time.time)
            fla.debanDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            mongoLoginAccountRepository.save(fla)
            log().info("LoginAccount: ${fla.username} is banned until ${banDate.toString()}")
            log().warn("Deban date for account: ${fla.username} have been updated in db to: $instant")
        }
    }

    private fun checkDefinitiveBan(pageSource: String, facebookLoginAccount: FacebookLoginAccount): LocalDateTime? {
        if(StringMatcher.containsAny(pageSource, "temporalyblocked")) {
            val byebye = Date(16752355200000L)
            val ldt = LocalDateTime.ofInstant(byebye.toInstant(), ZoneId.systemDefault())
            facebookLoginAccount.debanDate = ldt
            mongoLoginAccountRepository.save(facebookLoginAccount)
            log().error("Account: ${facebookLoginAccount.username} is banned definitively.")
            return ldt
        }
        return null
    }

    private fun getFacebookLoginName(doc: Element): String? {
        var profilo = doc.getElementsByAttributeValue("title", "Profilo")
        if(profilo.isEmpty())
            profilo = doc.getElementsByAttributeValue("title", "Profile")
        return if (profilo.isNotEmpty()) {
            extractFacebookNameFromHref(profilo.attr("href"))!!
        }
        else null
    }
}