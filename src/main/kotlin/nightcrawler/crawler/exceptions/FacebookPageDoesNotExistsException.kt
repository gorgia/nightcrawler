package nightcrawler.crawler.exceptions

/**
 * Created by andrea on 23/03/17.
 */


class FacebookPageDoesNotExistsException(url : String) : Exception("FacebookPage does not exists: $url") {
}