package nightcrawler.facebook.info.infograbber.infograbbers.jsoupextractors

import org.jsoup.nodes.Element

/**
 * Created by andrea on 26/07/16.
 */

interface JsoupDataExtractor {
    fun extractData(element: Element): Any
}