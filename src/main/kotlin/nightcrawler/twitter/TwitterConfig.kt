package nightcrawler.twitter

import nightcrawler.twitter.utils.MyTwitterFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import twitter4j.Twitter

/**
 * Created by andrea on 01/02/17.
 */
@Configuration
class TwitterConfig {

    @Autowired
    lateinit var myTwitterFactory: MyTwitterFactory


    @Bean
    fun getTwitter(): Twitter {
        return myTwitterFactory.getTwitter()
    }

}