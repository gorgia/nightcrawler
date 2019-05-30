package nightcrawler.facebook.info.infograbber.timeline

import nightcrawler.crawler.commands.WebDriverCommand
import nightcrawler.crawler.commands.CLICK
import nightcrawler.crawler.commands.CommandFactory
import nightcrawler.database.neo4j.models.FacebookEntity
import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.database.neo4j.models.FacebookPost
import nightcrawler.database.neo4j.repositories.FacebookPostRepository
import nightcrawler.facebook.info.FacebookNeo4jEntityFactory
import nightcrawler.facebook.info.infograbber.infograbbers.AtomicFacebookPageReacher
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.LikesExtractorFacebook
import nightcrawler.facebook.info.infograbber.timeline.jsoupextractors.PostLikesExtractor
import nightcrawler.utils.log
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.openqa.selenium.By
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by andrea on 05/08/16.
 */
@Component
@Scope("prototype")
class PostLikesGrabber : Processor, AtomicFacebookPageReacher() {


    @Autowired
    lateinit var facebookNeo4jEntityFactory: FacebookNeo4jEntityFactory

    @Autowired
    lateinit var facebookPostRepository: FacebookPostRepository


    @Autowired
    @Qualifier("likesExtractorFacebook")
    lateinit var dataExtractor: LikesExtractorFacebook

    override fun process(exchange: Exchange?) {
        val post = exchange?.`in`!!.body as FacebookPost?
        val likesUrl = exchange.`in`!!.getHeader("likesUrl") as String
        if (post != null) {
            try {
                updateFacebookPost(post, extractDataFromUrl(likesUrl))
            } catch(e: Exception) {
                log().error("Error during process of likes at likesUrl: $likesUrl", e)
            }
        }
    }


    fun extractDataFromUrl(url: String): HashMap<String, Element> {
        log().debug("Grabbing POSTLIKES from url $url")
        val pageSource = specialGetPage(url)
        return PostLikesExtractor().extractData(Jsoup.parse(pageSource))
    }


    fun updateFacebookPost(post: FacebookPost, facebookEntitiesMap: HashMap<String, Element>) {
        if (post.likes.isNotEmpty()) {
            val oldPostLikesFacebookIds: List<String?> = post.likes.map { likesEntity: FacebookEntity -> likesEntity.facebookId }//facebookAccount.followers.map({likesEntity : FacebookEntity -> likesEntity.facebookId})
            facebookEntitiesMap.filterKeys { key -> !oldPostLikesFacebookIds.contains(key) }
        }
        post.likes.addAll(facebookNeo4jEntityFactory.getOrCreateFacebookPageFromElements(FacebookPage::class.java, facebookEntitiesMap.values))
        facebookPostRepository.save(post, 1)
    }

    fun specialGetPage(url: String): String? {
        val commandsList: LinkedList<WebDriverCommand> = LinkedList()
        commandsList.add(CommandFactory.createCommand(CLICK::class.java, mapOf(Pair("by", By.className("uiMorePagerPrimary")), Pair("cycle", 10), Pair ("waitSeconds", 5))))
        return getPageWithCommands(url, *commandsList.toTypedArray())
    }


}