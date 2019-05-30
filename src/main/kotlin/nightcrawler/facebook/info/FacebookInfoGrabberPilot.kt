package nightcrawler.facebook.info

import kotlinx.coroutines.experimental.Runnable
import nightcrawler.facebook.targetmanager.MongoTargetManager
import nightcrawler.utils.log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component

/**
 * Created by andrea on 10/08/16.
 */
@Component
@ConditionalOnProperty(value = ["scheduling.enabled"], havingValue = "true", matchIfMissing = true)
class FacebookInfoGrabberPilot : Runnable {

    @Autowired
    private lateinit var ctx: ApplicationContext

    @Autowired
    lateinit var infoGrabberExecutor: ThreadPoolTaskExecutor

    @Autowired
    lateinit var targetManager: MongoTargetManager


    @Scheduled(initialDelay=10000, fixedDelay = 5000)
    override fun run() {
        if (infoGrabberExecutor.activeCount < infoGrabberExecutor.corePoolSize) {
            log().info("Ready to process a new target.")
            val target = targetManager.getNextTarget()
            log().info("Target account picked is: ${target.facebookIdentifier}")
            infoGrabberExecutor.execute(ctx.getBean(FacebookInfoGrabberRunnable::class.java, target))
        }
    }
}



