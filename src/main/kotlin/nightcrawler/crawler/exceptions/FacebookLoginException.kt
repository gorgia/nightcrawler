package nightcrawler.crawler.exceptions

import nightcrawler.database.mongodb.models.FacebookLoginAccount

/**
 * Created by andrea on 02/01/17.
 */
class FacebookLoginException(fla : FacebookLoginAccount) : Exception("Impossibile to login with cookies with account: ${fla.username}. There are not cookies in DB.") {
}