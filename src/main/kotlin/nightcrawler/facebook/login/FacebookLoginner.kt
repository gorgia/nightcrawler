package nightcrawler.facebook.login

import nightcrawler.crawler.Crawler
import nightcrawler.crawler.utils.ExternalIpExtractor
import nightcrawler.crawler.webdriver.getCookies
import nightcrawler.crawler.webdriver.takeScreenshot
import nightcrawler.database.mongodb.models.FacebookLoginAccount
import nightcrawler.database.mongodb.repositories.FacebookLoginAccountRepository
import nightcrawler.facebook.info.bancheck.FacebookBanCheckerFacebook
import nightcrawler.facebook.info.infograbber.exceptions.AccountBlockedPermanentlyException
import nightcrawler.springconfiguration.ApplicationContextProvider
import nightcrawler.utils.StringMatcher
import nightcrawler.utils.log
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by andrea on 27/07/16.
 */
@Component
@Scope("singleton")
class FacebookLoginner {


    @Autowired
    lateinit var flam: FacebookLoginAccountManager

    @Autowired
    lateinit var flar: FacebookLoginAccountRepository

    internal val facebookBaseUrl = "https://www.facebook.com"

    fun login(crawler: Crawler, loginWithCookies: Boolean = true): FacebookLoginAccount {
        return login(crawler, ExternalIpExtractor.getExternalIp(crawler), loginWithCookies)
    }

    fun login(crawler: Crawler, ip: String, loginWithCookies: Boolean = true): FacebookLoginAccount {
        var availableAccounts = flam.getAllAvailableAccountByIp(ip)
        if (availableAccounts.isEmpty()) {
            log().error("No Accounts are available for ip: $ip")
            availableAccounts = flam.getAllAvailableAccounts()
        }
        Collections.shuffle(availableAccounts)
        val facebookLoginAccount = availableAccounts.first()
        try {
            login(crawler, facebookLoginAccount, loginWithCookies)
        } catch(e: Exception) {
            log().error("Impossibile to log in with account: ${facebookLoginAccount.facebookName}")
            throw e
        }
        return facebookLoginAccount
    }

    fun login(crawler: Crawler, fla: FacebookLoginAccount, loginWithCookies: Boolean = false) {
        var isLogged = false
        if (loginWithCookies) {
            if (fla.loginCookies?.isNotEmpty() == true) {
                isLogged = this.loginWithCookies(crawler.webDriver, fla)
            } else {
                log().warn("Impossibile to login with cookies with account: ${fla.username}. There are not cookies in DB.")
            }
        }
        if (!isLogged) {
            isLogged = loginWithUsernameAndPassword(crawler.webDriver, fla)
        }
        if (!isLogged)
            log().error("Impossible to login with user: ${fla.username}")
    }


    private fun loginWithUsernameAndPassword(webDriver: WebDriver, facebookLoginAccount: FacebookLoginAccount): Boolean {
        webDriver.get(facebookBaseUrl)
        if (isLogged(webDriver, fla = facebookLoginAccount)) {
            return true
        }
        return internalLoginWithUsernameAndPassword(webDriver, facebookLoginAccount)
    }


    private fun isLogged(webDriver: WebDriver, maxWaitSeconds: Int = 15, fla: FacebookLoginAccount? = null): Boolean {
        try {
            if (webDriver.manage().cookies.isEmpty()) return false //if there are not cookies how can it be logged in? returning false this way should be a lot faster
            val pageSource = webDriver.pageSource
            if(StringMatcher.containsAny(pageSource, "permanentblock"))
                throw AccountBlockedPermanentlyException(fla?.facebookName ?: "")
            val wait = WebDriverWait(webDriver, maxWaitSeconds.toLong())
            val elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[@role=\"navigation\"]")))//(By.xpath("//*[starts-with(@id,\"profile_pic_header_\")] | //*[contains(@class, \"checkpoint\")] ")))
            if (elements.isEmpty()) return false
            val facebookName = elements.first().findElements(By.tagName("span")).first().text
            if (facebookName == null || facebookName.length < 2) throw Exception("login error retry")
            //check if account is blocked permanently
            if (facebookName.contains("bloccato") || facebookName.contains("locked")) {
                throw AccountBlockedPermanentlyException(facebookName)
            }
            log().info("Thread " + Thread.currentThread() + " thread id: " + Thread.currentThread().id + " has successfully logged in Facebook as " + facebookName + " ")
            return true
        } catch (e: Exception) {
            if (e is AccountBlockedPermanentlyException) {
                log().error("Account should be permanently disabled")
                if (fla != null) permanetlybanFacebookLoginAccount(fla)
            }
            log().warn("Browser is not logged or is unable to reach login page for account ${fla?.username}")
            webDriver.takeScreenshot("error-screenshots/loginError.jpg")
        }
        return false
    }

    private fun permanetlybanFacebookLoginAccount(fla: FacebookLoginAccount) {
        fla.ips = HashSet()
        flar.save(fla)
    }

    fun loginWithCookies(webDriver: WebDriver, facebookLoginAccount: FacebookLoginAccount): Boolean {
        try {
            webDriver.get("https://www.facebook.com")
            facebookLoginAccount.loginCookies?.forEach { webDriver.manage().addCookie(it) }
            webDriver.navigate()?.refresh()
            if (isLogged(webDriver, fla = facebookLoginAccount)) {
                log().info("Logged successfully with account: ${facebookLoginAccount.username}")
                return true
            }
        } catch(e: Exception) {
            log().warn("Impossible to login with cookies. Reverting to normal login for account: ${facebookLoginAccount.username}")
        }
        return false
    }


    private fun internalLoginWithUsernameAndPassword(webDriver: WebDriver, facebookLoginAccount: FacebookLoginAccount): Boolean {
        log().debug("Try to login in Facebook as: ${facebookLoginAccount.username}")
        var element = webDriver.findElement(By.cssSelector("#email"))
        element?.sendKeys(facebookLoginAccount.username)
        element = webDriver.findElement(By.id("pass"))
        element?.sendKeys(facebookLoginAccount.password)
        element.sendKeys(Keys.RETURN)
        Thread.sleep(3000)
        //hit RETURN and wait for specific subElement to show up
        if (isLogged(webDriver, fla = facebookLoginAccount)) {
            log().debug("Saving cookies for account: ${facebookLoginAccount.username}")
            facebookLoginAccount.loginCookies = webDriver.getCookies()
            flar.save(facebookLoginAccount)
            return true
        }
        return false
    }


}