package nightcrawler.springconfiguration

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * Created by andrea on 26/11/15.
 */

/**
 * Created by andrea on 30/03/15.
 */
@Component
object ApplicationContextProvider : ApplicationContextAware {

    var ctx: ApplicationContext? = null


    override fun setApplicationContext(applicationContext: ApplicationContext?) {
        ctx = applicationContext
    }
}
