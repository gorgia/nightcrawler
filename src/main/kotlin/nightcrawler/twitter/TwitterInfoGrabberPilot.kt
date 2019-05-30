package nightcrawler.twitter

import nightcrawler.twitter.databases.mongo.TwitterTargetManager
import nightcrawler.twitter.info.TwitterInfoGrabber
import nightcrawler.utils.log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Executors

/**
 * Created by andrea on 07/02/17.
 */
@Component
@ConditionalOnProperty(value = ["scheduling.enabled"], havingValue = "true", matchIfMissing = true)
open class TwitterInfoGrabberPilot {

    @Autowired
    lateinit var twitterTargetManager: TwitterTargetManager

    @Autowired
    lateinit var applicationContext: ApplicationContext


    val singleThreadExecutor = Executors.newSingleThreadExecutor()

    //@Scheduled(initialDelay = 10000, fixedDelay = 60000)
    //@Async("singleThreadExecutor")
    fun executeInfoGrabber() {
        try {
            val tt = twitterTargetManager.getNextTwitterTarget()
            applicationContext.getBean(TwitterInfoGrabber::class.java, tt).run()
        } catch(e: Exception) {
            log().error("Error in twitter info grabber", e)
        }
    }

}