package nightcrawler.springconfiguration

import nightcrawler.crawler.webdriver.WebDriverFactory
import nightcrawler.utils.conf
import org.openqa.selenium.WebDriver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

/**
 * Created by andrea on 06/02/17.
 */

@Configuration
open class BeanFactoryConfiguration {
    @Bean
    @Scope(value = "prototype")
    @Lazy
    open fun getWebDriver(): WebDriver {
        return WebDriverFactory.create()
    }


    @Bean
    open fun infoGrabberExecutor(): ThreadPoolTaskExecutor {
        val pool = ThreadPoolTaskExecutor()
        pool.corePoolSize = conf.getInt("socialnet.parallelInfoGrabbers")
        pool.maxPoolSize = conf.getInt("socialnet.parallelInfoGrabbers")
        pool.threadNamePrefix = "infoGrabber"
        pool.setQueueCapacity(Integer.MAX_VALUE)
        pool.setWaitForTasksToCompleteOnShutdown(false)
        return pool
    }

}