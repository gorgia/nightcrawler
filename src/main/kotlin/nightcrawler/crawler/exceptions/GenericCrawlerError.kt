package nightcrawler.crawler.exceptions

/**
 * Created by andrea on 28/10/16.
 */
class GenericCrawlerError(e: Exception) : CrawlerException("Generic Cralwer Error: ${e::class.java}. Please Retry!"){
}