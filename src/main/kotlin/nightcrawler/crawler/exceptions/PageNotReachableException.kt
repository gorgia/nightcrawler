package nightcrawler.crawler.exceptions

/**
 * Created by andrea on 25/10/16.
 */
class PageNotReachableException(url : String) : CrawlerException("page not reachable: $url")