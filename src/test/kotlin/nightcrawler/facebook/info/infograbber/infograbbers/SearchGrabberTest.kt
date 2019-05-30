package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.TestConfiguration
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.util.concurrent.Executors


@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = ["scheduling.enabled=false"], classes = arrayOf(TestConfiguration::class))
class SearchGrabberTest {

    @Autowired
    lateinit private var ctx: ApplicationContext

    @Test
    fun test(){
        val testUrl = "https://www.facebook.com/search/1178933415531366/likers/211401918930049/likers/intersect"
        val searchGrabber = ctx.getBean(SearchGrabber::class.java)
        assertTrue(searchGrabber.process(testUrl).isNotEmpty())
    }
}