package nightcrawler

import nightcrawler.utils.log
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import kotlin.test.assertNotNull


/**
 * Created by andrea on 06/02/17.
 */

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = arrayOf("scheduling.enabled=false"), classes = arrayOf(TestConfiguration::class))
open class TestTestConfiguration {

    @Autowired
    lateinit var appContext: ApplicationContext

    @Autowired
    lateinit var infoGrabberExecutor: ThreadPoolTaskExecutor

    @Test
    fun testInfoGrabberCreation() {
        log().info("Scheduling enabled: ${appContext.environment.getProperty("scheduling.enabled")}")
        assertNotNull(infoGrabberExecutor)
    }
}