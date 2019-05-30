package nightcrawler.facebook.login

import com.uwyn.jhighlight.fastutil.Hash
import nightcrawler.TestConfiguration
import nightcrawler.crawler.Crawler
import nightcrawler.database.mongodb.models.FacebookLoginAccount
import nightcrawler.database.mongodb.repositories.FacebookLoginAccountRepository
import nightcrawler.facebook.info.infograbber.infograbbers.TelephoneGrabber
import nightcrawler.springconfiguration.ApplicationContextProvider
import nightcrawler.utils.log
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.util.*
import java.util.concurrent.Future
import kotlin.test.assertTrue

/**
 * Created by andrea on 06/02/17.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = ["scheduling.enabled=false"], classes = [(TestConfiguration::class)])
class FacebookLoginnerTest {


    val loginAccountId = "marina.brognoli666"
    val telephoneNumberForTest = "393286484744"

    @Autowired
    lateinit private var ctx: ApplicationContext

    @Autowired
    lateinit var facebookLoginAccountRepo : FacebookLoginAccountRepository

    @Autowired
    lateinit var facebookLoginAccountManager : FacebookLoginAccountManager

    @Autowired
    lateinit var facebookLoginner : FacebookLoginner

    @Autowired
    lateinit var infoGrabberExecutor: ThreadPoolTaskExecutor

    @Test
    @Ignore
    fun testLoginWithCookies(){
        val loginAccount = facebookLoginAccountRepo.findById(loginAccountId).get()
        //val loginAccount = facebookLoginAccountRepo.findOne(loginAccountId)
        val crawler = ApplicationContextProvider.ctx!!.getBean(Crawler::class.java)
        assertTrue(facebookLoginner.loginWithCookies(crawler.webDriver,loginAccount))
    }

    @Test //determine if account is enabled to search telephones
    fun testAllTelephoneGrabberEnabled(){
       val accountList = facebookLoginAccountManager.getAllFunctioningAccount()
        val futureMap : MutableMap<FacebookLoginAccount, Future<*>> = HashMap()
        infoGrabberExecutor.setWaitForTasksToCompleteOnShutdown(true)
        accountList.forEach {
            futureMap.put(it,infoGrabberExecutor.submit(ctx.getBean(TelephoneGrabber::class.java, telephoneNumberForTest, false, it)))
        }
        futureMap.forEach { t, u ->
            try {
                val targetFacebookId = u.get()
                if (targetFacebookId != null) {
                    t.telephoneNumberFinderEnabled = true
                    log().info("account ${t.username} can do telephone search")
                    facebookLoginAccountRepo.save(t)
                }
            }catch(e: Exception){
                log().error("Error for facebookAccount $t ", e)
            }
        }
        infoGrabberExecutor.shutdown()
    }


}