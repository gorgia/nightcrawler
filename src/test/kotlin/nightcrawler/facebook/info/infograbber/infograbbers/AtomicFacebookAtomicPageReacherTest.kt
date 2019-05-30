package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.ApplicationNightcrawler
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.FriendsExtractorFacebook
import org.jsoup.Jsoup
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * Created by andrea on 03/01/17.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(ApplicationNightcrawler::class))
class AtomicFacebookAtomicPageReacherTest {


    val targetUrl1 = "https://www.facebook.com/renatoderosa/friends?lst=100003661309421%3A765159690%3A1483428032&source_ref=pb_friends_tl"

    val targetUrl2 = "https://www.facebook.com/ernesto.dazzini/friends?lst=100003661309421%3A100004917065261%3A1483428443&source_ref=pb_friends_tl"

    @Autowired
    lateinit var atomicPageReacherTrue: AtomicFacebookPageReacher

    @Test
    fun getPageFriendsTest(){
        var html = atomicPageReacherTrue.getPage(targetUrl1)
        var elements  = FriendsExtractorFacebook().extractData(Jsoup.parse(html))
        assertTrue(elements.isNotEmpty())
        html = atomicPageReacherTrue.getPage(targetUrl2)
        elements = FriendsExtractorFacebook().extractData(Jsoup.parse(html))
        assertTrue(elements.isNotEmpty())
        Thread.sleep(10000)
    }
}