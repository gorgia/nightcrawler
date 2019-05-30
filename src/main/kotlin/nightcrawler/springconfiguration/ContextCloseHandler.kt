package nightcrawler.springconfiguration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component

/**
 * Created by andrea on 22/08/16.
 */

@Component
open class ContextCloseHandler : ApplicationListener<ContextClosedEvent> {
    @Autowired
    lateinit var infoGrabberExecutor: ThreadPoolTaskExecutor

    override fun onApplicationEvent(event: ContextClosedEvent?) {
        infoGrabberExecutor.shutdown()
    }

}