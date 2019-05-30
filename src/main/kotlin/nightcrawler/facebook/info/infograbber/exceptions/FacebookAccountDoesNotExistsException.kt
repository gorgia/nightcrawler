package nightcrawler.facebook.info.infograbber.exceptions

import nightcrawler.database.neo4j.models.FacebookPage

/**
 * Created by andrea on 08/08/16.
 */
class FacebookAccountDoesNotExistsException(message: String) : Exception(message) {

    constructor(facebookPage: FacebookPage) : this("FacebookAccount ${facebookPage.facebookId} does not exists") {
    }
}

