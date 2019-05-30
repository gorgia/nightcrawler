package nightcrawler.facebook.info.infograbber.timeline

import nightcrawler.crawler.Crawler
import nightcrawler.database.neo4j.models.FacebookEntity
import nightcrawler.database.neo4j.models.FacebookPost
import nightcrawler.database.neo4j.repositories.FacebookPostRepository
import nightcrawler.facebook.info.FacebookNeo4jEntityFactory
import nightcrawler.facebook.info.infograbber.infograbbers.AtomicFacebookPageReacher
import nightcrawler.facebook.login.FacebookLoginner
import nightcrawler.springconfiguration.ApplicationContextProvider
import nightcrawler.utils.log
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Created by andrea on 05/08/16.
 */
@Component
@Scope("prototype")
class PostSharesGrabber : Processor, AtomicFacebookPageReacher(){

    @Autowired
    lateinit var facebookNeo4jEntityFactory: FacebookNeo4jEntityFactory

    @Autowired
    lateinit var facebookPostRepository: FacebookPostRepository


    @Autowired
    lateinit var loginner: FacebookLoginner


    override fun process(exchange: Exchange?) {
        var shareUrl: String? = null
        try {
            val post = exchange!!.`in`?.getBody(FacebookPost::class.java)
            if (post != null) {
                log().debug("Grabbing POSTSHARES for post ${post.url}")
                shareUrl = "https://www.facebook.com/shares/view?id=${post.facebookId}"
                log().debug("Extracting shares from postUrl: ${post.url}")
                val timelineExtractor = ApplicationContextProvider.ctx!!.getBean(TimelineGrabber::class.java, shareUrl)
                updateFacebookPost(post, timelineExtractor.process(eagerTimeline = true, eagerComments = false)?.toHashSet()!!)
            }
        } catch (e: Exception) {
            log().error("Exception during processing of shareUrl: $shareUrl", e)
            if (e is RuntimeException)
                log().error("Exception cause $e")
        }

    }


    fun updateFacebookPost(post: FacebookPost, shares: Set<FacebookPost>) {
        if (post.shares.isNotEmpty()) {
            val oldSharesId: List<String?> = post.shares.map { likesEntity: FacebookEntity -> likesEntity.facebookId }//facebookAccount.followers.map({likesEntity : FacebookEntity -> likesEntity.facebookId})
            shares.filter { share -> !oldSharesId.contains(share.facebookId) }
        }
        post.shares.addAll(shares)
        facebookPostRepository.save(post, 1)
    }


}