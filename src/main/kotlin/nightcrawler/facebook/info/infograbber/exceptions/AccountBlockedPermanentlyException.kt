package nightcrawler.facebook.info.infograbber.exceptions

import nightcrawler.database.mongodb.models.FacebookLoginAccount

/**
 * Created by andrea on 10/08/16.
 */

open class AccountBlockedPermanentlyException(facebookLoginAccountName: String) : Exception("Account $facebookLoginAccountName blocked permanently! ") {
    constructor(fla: FacebookLoginAccount) : this(fla.facebookName!!)
}
