package nightcrawler.facebook.info.infograbber.infograbbers

import nightcrawler.crawler.exceptions.PageNotReachableException
import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.facebook.info.FacebookNeo4jEntityFactory
import nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors.JsoupDataExtractor
import nightcrawler.utils.log
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.openqa.selenium.By
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

/**
 * Created by andrea on 05/08/16.
 */

abstract class InfoGrabberCommandBase(val scrollBy: By? = null, val attributeToCheck : String? = null, val containingAttribute : String? = null) : AtomicFacebookPageReacher(), InitializingBean {
    abstract var dataExtractor: JsoupDataExtractor
    abstract val facebookPage: FacebookPage


    lateinit var url: String

    @Autowired
    lateinit var facebookNeo4jEntityFactory: FacebookNeo4jEntityFactory

    fun process() {
        try {
            log().debug("Start grabbing data from url: $url")
            if (!hasAttribute()) {
                log().info("Page $url does not contains required attribute $attributeToCheck with value: $containingAttribute. Skip infograbber: ${this.javaClass.simpleName}")
                return
            }
            val pageSource = getPage(url, scrollBy = scrollBy, sleepSeconds = 5)
            val extractorResult: LinkedHashMap<String, Element> = dataExtractor.extractData(Jsoup.parse(pageSource)) as LinkedHashMap<String, Element>
            log().info("Found ${extractorResult.count()} entitites at url:$url")
            if (extractorResult.isNotEmpty()) {
                updateFacebookAccount(extractorResult)
            }
        } catch (e: Exception) {
            if(e is PageNotReachableException){
                log().error("Error occurred grabbing url: $url")
            }
            log().error("", e)
        }
    }

    fun hasAttribute(): Boolean {
        if (attributeToCheck == null) return true
        try {
            val pageSource = getPage(url)
            val doc = Jsoup.parse(pageSource)!!
            if(attributeToCheck!= null && containingAttribute == null){
                return doc.getElementsByAttribute(attributeToCheck).isNotEmpty()
            }
            else if(attributeToCheck!=null && containingAttribute != null)
                return doc.getElementsByAttributeValueContaining(attributeToCheck, containingAttribute).isNotEmpty()
        } catch (e: Exception) {
            log().error("Error during hasAttribute in InfoGrabberCommandBase")
        }
        return false
    }

    abstract fun updateFacebookAccount(facebookEntitiesMap: LinkedHashMap<String, Element>)

    abstract fun createUrl(): String

    override fun afterPropertiesSet() {
        this.url = createUrl()
    }

}