package nightcrawler.facebook.login

import com.google.common.util.concurrent.AtomicLongMap
import nightcrawler.TestConfiguration
import nightcrawler.utils.log
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import kotlin.test.assertFalse


/**
 * Created by andrea on 09/08/16.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = arrayOf("scheduling.enabled=false"), classes = arrayOf(TestConfiguration::class))
open class FacebookTwitterLoginAccountManagerTest {
    @Autowired
    lateinit var facebookLoginAccountManager: FacebookLoginAccountManager

    @Test
    open fun test() {
        val accounts = facebookLoginAccountManager.getAllAvailableAccounts()
        val map = AtomicLongMap.create<String>()
        accounts.forEach { it.ips.forEach { map.getAndIncrement(it) } }
        log().info(map.asMap().toString())
        assertFalse(map.isEmpty)
    }

}