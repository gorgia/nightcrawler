package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.ApplicationNightcrawler
import nightcrawler.springconfiguration.ApplicationContextProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * Created by andrea on 23/08/16.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(ApplicationNightcrawler::class))
class GroupMembersGrabberTest(){



    @Test
    fun test(){
        val grabber = ApplicationContextProvider.ctx?.getBean(GroupMembersGrabber::class.java)


    }
}