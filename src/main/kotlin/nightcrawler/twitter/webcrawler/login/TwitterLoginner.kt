package nightcrawler.twitter.webcrawler.login

import nightcrawler.crawler.Crawler
import nightcrawler.crawler.webdriver.getCookies
import nightcrawler.database.mongodb.models.FacebookLoginAccount
import nightcrawler.twitter.databases.mongo.models.TwitterLoginAccount
import nightcrawler.twitter.databases.mongo.repo.TwitterLoginAccountRepository
import nightcrawler.utils.log
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import thredds.catalog.crawl.CatalogCrawler
import java.util.*

/**
 * Created by andrea on 27/02/17.
 */
@Component
open class TwitterLoginner {

    val twitterBaseUrl = "https://twitter.com"
    val twitterLoginUrl = "https://twitter.com/login"


    @Autowired
    lateinit var twitterLoginAccountManager: TwitterLoginAccountManager


    open fun login(crawler: Crawler) {
        login(crawler.webDriver)
    }

    open fun login(webDriver: WebDriver) {
        val randLoginAccount = twitterLoginAccountManager.getRandomLoginAccount()
        var logged: Boolean = false
        if (randLoginAccount.loginCookies.isNotEmpty()) {
            logged = loginWithCookies(webDriver, randLoginAccount)
        }
        if (!logged) loginWithUsernameAndPassword(webDriver, randLoginAccount)
    }

    open fun loginWithUsernameAndPassword(webDriver: WebDriver, twitterLoginAccount: TwitterLoginAccount): Boolean {
        webDriver.get(twitterLoginUrl)
        if (isLogged(webDriver)) {
            return true
        }
        return internalLoginWithUsernameAndPassword(webDriver, twitterLoginAccount)
    }


    private fun internalLoginWithUsernameAndPassword(webDriver: WebDriver, twitterLoginAccount: TwitterLoginAccount): Boolean {
        log().debug("Try to login in Facebook as: ${twitterLoginAccount.mail}")
        var element = webDriver.findElement(By.className("email-input"))
        element?.sendKeys(twitterLoginAccount.mail)
        element = webDriver.findElement(By.className("js-password-field"))
        element?.sendKeys(twitterLoginAccount.password)
        Thread.sleep(3000)
        element.sendKeys(Keys.RETURN)
        //hit RETURN and wait for specific subElement to show up
        if (isLogged(webDriver)) {
            log().debug("Saving cookies for account: ${twitterLoginAccount.mail}")
            twitterLoginAccount.loginCookies = webDriver.getCookies()
            twitterLoginAccountManager.saveTwitterLoginAccount(twitterLoginAccount)
            return true
        }
        return false
    }

    open fun isLogged(webDriver: WebDriver): Boolean {
        return webDriver.findElements(By.id("user-dropdown")).isNotEmpty()
    }



    fun loginWithCookies(webDriver: WebDriver, twitterLoginAccount: TwitterLoginAccount): Boolean {
        try {
            webDriver.get(twitterBaseUrl)
            twitterLoginAccount.loginCookies.forEach { webDriver.manage().addCookie(it) }
            webDriver.navigate()?.refresh()
            if (isLogged(webDriver)) {
                log().info("Logged successfully with cookies of account: ${twitterLoginAccount.mail}")
                return true
            }
        } catch(e: Exception) {
            log().warn("Impossible to login with cookies. Reverting to normal login for account: ${twitterLoginAccount.mail}")
        }
        return false
    }
}