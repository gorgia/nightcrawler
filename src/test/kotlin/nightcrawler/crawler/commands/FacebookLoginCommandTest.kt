package nightcrawler.crawler.commands

import nightcrawler.ApplicationNightcrawler
import nightcrawler.crawler.Crawler
import nightcrawler.database.mongodb.repositories.FacebookLoginAccountRepository
import nightcrawler.facebook.login.FacebookLoginner
import nightcrawler.springconfiguration.ApplicationContextProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * Created by andrea on 22/07/16.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(ApplicationNightcrawler::class))
open class FacebookLoginCommandTest {

    @Autowired
    lateinit var facebookLoginAccountRepository: FacebookLoginAccountRepository

    @Autowired
    lateinit var facebookLoginner: FacebookLoginner

    @Test
    fun execute() {
        val facebookloginAccounts = facebookLoginAccountRepository.findAll()
        facebookloginAccounts.forEach {
            if (it.ips.isNotEmpty()) {
                var crawler = ApplicationContextProvider.ctx!!.getBean(Crawler::class.java)
                facebookLoginner.login(crawler!!, it)
                crawler.close()
                crawler = ApplicationContextProvider.ctx!!.getBean(Crawler::class.java)
                facebookLoginner.login(crawler!!, it, true)
                crawler.close()
            }
        }


    }

}