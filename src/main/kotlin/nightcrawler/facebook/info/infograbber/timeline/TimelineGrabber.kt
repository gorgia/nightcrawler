package nightcrawler.facebook.info.infograbber.timeline

import nightcrawler.database.neo4j.models.FacebookPost
import nightcrawler.database.neo4j.splitLoadersAndSavers.FacebookSplitSaver
import nightcrawler.facebook.info.FacebookNeo4jEntityFactory
import nightcrawler.facebook.info.infograbber.infograbbers.AtomicFacebookPageReacher
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.JsoupDataExtractor
import nightcrawler.facebook.info.infograbber.timeline.jsoupextractors.TimelineJsoupExtractor
import nightcrawler.utils.log
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.openqa.selenium.By
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by andrea on 09/08/16.
 */
@Component
@Scope("prototype")
class TimelineGrabber(val url: String) : AtomicFacebookPageReacher() {


    val postElementsExtractor: JsoupDataExtractor = TimelineJsoupExtractor()

    @Autowired
    lateinit var facebookEntityFactory: FacebookNeo4jEntityFactory

    @Autowired
    lateinit var facebookSplitSaver: FacebookSplitSaver


    @Autowired
    lateinit var postDataGrabber: PostDataGrabber

    fun process(eagerTimeline: Boolean = false, eagerComments: Boolean = false): Set<FacebookPost> {
        log().debug("Grabbing Timeline at url: $url")
        val posts = HashSet<FacebookPost>()
        try {
            var postElements: HashMap<String, Element>
            val pageSource = getPage(url, scrollNecessary = eagerTimeline, scrollBy = By.className("userContentWrapper"))!!
            postElements = postElementsExtractor.extractData(Jsoup.parse(pageSource)) as HashMap<String, Element>
            if (postElements.isEmpty()) return posts

            if (facebookEntityFactory.getByFacebookId(postElements.keys.first(), FacebookPost::class.java) != null && !eagerTimeline) {
                log().info("Last Post at $url is already in neo4j: no need to crawl this timeline")
                return posts
            }

            postElements = postElementsExtractor.extractData(Jsoup.parse(pageSource)) as HashMap<String, Element>
            postElements.forEach { k, pe ->
                val facebookPostAlreadyInDB = facebookEntityFactory.getByFacebookId(k, FacebookPost::class.java)
                if (facebookPostAlreadyInDB != null && !eagerTimeline) log().debug("FacebookPost with facebookId ${facebookPostAlreadyInDB.facebookId} is already in db. No further processing needed")
                else {
                    val post = postDataGrabber.extractData(k, pe, eagerComments)
                    facebookSplitSaver.save(post)
                    posts.add(post)
                }
            }
        } catch(e: Exception) {
            log().error("Error during timelineGrabber at url: $url", e)
        }
        return posts
    }

}