package nightcrawler.facebook.info.bancheck

import nightcrawler.ApplicationNightcrawler
import nightcrawler.crawler.Crawler
import nightcrawler.database.mongodb.models.FacebookLoginAccount
import nightcrawler.database.mongodb.repositories.FacebookLoginAccountRepository
import nightcrawler.utils.log
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * Created by andrea on 08/08/16.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(ApplicationNightcrawler::class))
open class FacebookBanCheckerTest {


    @Autowired
    lateinit var ctx: ApplicationContext

    @Autowired
    lateinit var facebookLoginAccountRepo: FacebookLoginAccountRepository

    @Test
    open fun testAllAccount() {
        val accounts: List<FacebookLoginAccount> = facebookLoginAccountRepo.findAll()
        accounts.forEach { account ->
            val crawler = ctx.getBean(Crawler::class.java)
            try {
                val banChecker = ctx.getBean(FacebookBanCheckerFacebook::class.java, crawler)
                banChecker.checkHasBanProblems(account)
            } catch(e: Exception) {
                log().error("", e)
            } finally {
                crawler.webDriver.quit()
            }
        }
    }

}