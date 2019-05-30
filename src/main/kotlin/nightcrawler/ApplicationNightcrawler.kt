package nightcrawler

import nightcrawler.crawler.webdriver.WebDriverFreeShutdownHook
import nightcrawler.utils.StringMatcher
import nightcrawler.utils.log
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration
import org.springframework.scheduling.annotation.EnableScheduling


/**
 * Created by andrea on 22/07/16.
 */
@SpringBootApplication(exclude = [(Neo4jDataAutoConfiguration::class)])
@EnableScheduling
class ApplicationNightcrawler : InitializingBean{
    override fun afterPropertiesSet() {
        log().info("main spring application context started")
    }
}

fun main(args: Array<String>) {
    println("Application NightCrawler starting... ")
    Runtime.getRuntime().addShutdownHook(Thread(WebDriverFreeShutdownHook()))
    SpringApplication.run(ApplicationNightcrawler::class.java, *args)
}
