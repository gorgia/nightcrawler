package nightcrawler.database.mongodb.models


import nightcrawler.crawler.webdriver.Cookie
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

/**
 * Created by andrea on 22/07/16.
 */
@Document
open class FacebookLoginAccount {
    @Id
    var username: String? = null
    var password: String? = null
    var facebookName: String? = null
    var loginCookies: Set<Cookie>? = null
    var ips: Set<String> = HashSet()
    var debanDate: LocalDateTime? = null
    var telephoneNumberFinderEnabled : Boolean? = null
}