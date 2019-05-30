package nightcrawler.facebook.info.infograbber.timeline

import nightcrawler.TestConfiguration
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * Created by andrea on 23/02/17.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = arrayOf("scheduling.enabled=false"), classes = arrayOf(TestConfiguration::class))
class TimelineGrabberTest{


    @Autowired
    lateinit var ctx : ApplicationContext

    val targetTimelineUrl = "https://www.facebook.com/karim.cheurfi.7"

    @Test
    fun testTimelineGrabber(){
        val timelineGrabber = ctx.getBean(TimelineGrabber::class.java, targetTimelineUrl)
        assertTrue(timelineGrabber.process(eagerTimeline = true, eagerComments = true).isNotEmpty())
    }}