package nightcrawler.twitter.databases.mongo.models

import nightcrawler.crawler.webdriver.Cookie
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * Created by andrea on 31/01/17.
 */
@Document
open class TwitterLoginAccount {
    @Id
    var twitterIdentifier : String? = null
    var password : String? = null
    var mail: String? = null
    var debug = true
    var consumerKey : String? = null
    var consumerSecret : String? = null
    var accessToken : String? = null
    var accessTokenSecret : String? = null
    var loginCookies: Set<Cookie> = HashSet()
    var isRateLimitedUntil: LocalDateTime? = null
}