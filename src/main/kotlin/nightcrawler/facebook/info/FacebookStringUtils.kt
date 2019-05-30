package nightcrawler.facebook.info

import nightcrawler.database.neo4j.models.FacebookEntity
import nightcrawler.database.neo4j.models.FacebookPage
import org.json.JSONObject
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.util.*
import java.util.regex.Pattern

/**
 * Created by andrea on 26/07/16.
 */


fun extractFacebookNameFromHref(href: String?): String? { //da rivedere per comprendere anche le pagine
    val log = LoggerFactory.getLogger("FacebookStringUtils")
    var name: String? = null
    try {
        if (href == null || href.length == 0) return null
        name = java.net.URLDecoder.decode(href, "UTF-8")
        if (name!!.contains("photo.php")) {
            if (name.contains("&")) {
                name = name.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            }
            if (name.contains(".")) {
                name = name.substring(name.lastIndexOf('.') + 1)
            }
            return name
        }
        if (!href.contains("profile.php?id=")) {
            name = name.split("?")[0]
        } else {
            name = name.split("&")[0]
        }
        if (name.contains("groups/"))
            name = name.split("groups/")[1]
        if (name.contains("pages/"))
            name = name.split("pages/")[1]
        if (name.contains("fref="))
            name = name.split("fref=")[0]
        if (name.contains("facebook.com/"))
            name = name.split("facebook.com/")[1]
        if (name.contains("/"))
            name = name.split("/")[0]
        if (name.contains("?") && !name.contains("profile.php?id="))
            name = name.split("\\?")[0]
        //name = name.toLowerCase();
    } catch (e: Exception) {
        log.error("Impossible to retrieve username from href: " + href!!, e)
    }
    return name?.toLowerCase()
}


fun extractIdFromHovercardAttribute(hovercardAttribute: String): String? {
    val log = LoggerFactory.getLogger("FacebookUtils")
    var id: String? = null
    try {
        if (hovercardAttribute.contains("id=")) {
            var partial = hovercardAttribute.split("id=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            if (partial.contains("&")) {
                partial = partial.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            }
            id = partial
        } else {
            val regex = Regex("\\d{5,}")
            id = regex.find(hovercardAttribute)?.groups?.get(0)?.value
        }
    } catch (ne: NullPointerException) {
        log.error("Error to be corrected yet")
    }
    if (id == null) return null
    try {
        if (id.matches("[0-9]+".toRegex()) && id.length > 2) {
            return id
        } else {
            log.error("extractIdFromHovercardAttribute failed for: " + hovercardAttribute)
        }
    } catch (npe: KotlinNullPointerException) {
        log.error("extractIdFromHovercardAttribute failed for: " + hovercardAttribute)
        throw npe
    }
    return null
}

fun baseUrlFromHref(hrefParam: String): String? {
    val log = LoggerFactory.getLogger("FacebookUtils")
    var href = hrefParam
    if (href.startsWith("https://") && href.contains("facebook.com/")) {
        if (href.isNotEmpty() && href[href.length - 1] == '/') {
            href = href.substring(0, href.length - 1)
        }
        if (!href.contains("profile.php?id=")) {
            href = href.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        } else if (href.contains("&")) {
            href = href.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        }
        return href.toLowerCase()
    }
    log.error("Account url does not looks right! href= $hrefParam")
    return href
}

fun getFirstRestSeparator(facebookName: String?): String {
    if (facebookName?.contains("profile.php?") ?: false)
        return "&sk="
    return "/"
}

fun getSecondRestSeparator(facebookName: String?): String {
    if (facebookName != null && (facebookName.contains("profile.php") || facebookName.matches("[0-9]+".toRegex()))) {
        return "&"
    }
    return "?"
}

fun baseUrlFromFacebookName(facebookName: String): String {
    if (!facebookName.startsWith("https://www.facebook.com/"))
        return "https://www.facebook.com/" + facebookName
    else return facebookName
}

fun baseUrlFromFacebookAccount(facebookEntity: FacebookEntity): String {
    if (facebookEntity.url != null) return baseUrlFromHref(facebookEntity.url!!)!!
    else if (facebookEntity is FacebookPage)
        return baseUrlFromFacebookName(facebookEntity.facebookName ?: facebookEntity.facebookId!!)
    else
        return baseUrlFromFacebookName(facebookEntity.facebookId!!)
}


fun fromUtimeStringToDate(utime: String): Date {
    return Date(java.lang.Long.parseLong(utime) * 1000)
}

fun extractFacebookIdFromElement(element: Element): String? {
    val log = LoggerFactory.getLogger("FacebookStringUtils")
    val subElement = element.getElementsByAttributeValueStarting("data-hovercard", "/ajax/hovercard")
    var facebookId: String? = null
    try {
        val datahovercard: String? = subElement.attr("data-hovercard")
        if (datahovercard!!.contains("id=")) {
            facebookId = extractIdFromHovercardAttribute(datahovercard)
        }
        if (facebookId == null)
            facebookId = extractFacebookNameFromHref(subElement.attr("href"))
    } catch (e: Exception) {
        log.error("Error during extraction of facebookId from subElement ${subElement.toString()}", e)
    }
    return facebookId
}

fun extractFacebookIdFromSearchElement(searchItem: Element): String? {
    val dataGtString = searchItem.getElementsByAttribute("data-bt").first()?.attr("data-bt") ?: return null
    val jsonObj = JSONObject(dataGtString)
    return jsonObj.get("id").toString()
}

fun extractFacebookNameFromSearchItem(searchItem: Element): String? {
    val href = searchItem.getElementsByAttributeValueStarting("href", "https://www.facebook.com/").first()?.attr("href") ?: return null
    return extractFacebookNameFromHref(href)
}

fun extractFacebookNameFromElement(element: Element): String? {
    val log = LoggerFactory.getLogger("FacebookStringUtils")
    val subElement = element.getElementsByAttributeValueContaining("href", "www.facebook.com")
    var facebookName: String? = null
    try {
        facebookName = extractFacebookNameFromHref(subElement.attr("href"))
    } catch (e: Exception) {
        log.error("Error during extraction of facebookId from subElement $subElement", e)
    }
    return facebookName
}


fun extractFacebookIdFromPostWrapper(wrapper: Element): String? {
    val hrefEl = wrapper.getElementsByClass("_5pcq")
    if (hrefEl.isEmpty()) return null
    val href = hrefEl?.first()?.attr("href")
    if (href != null) {
        return extractIdFromPostUrl(href)
    }
    return null
}

fun extractIdFromPostUrl(postUrl: String): String? {
    val pattern = Pattern.compile("[0-9]{6,}")
    val matcher = pattern.matcher(postUrl)
    val stringList = ArrayList<String>()
    while (matcher.find()) {
        stringList.add(matcher.group(matcher.groupCount()))
    }
    if (postUrl.contains("fbid") && !stringList.isEmpty())
        return stringList.get(0)
    if (!stringList.isEmpty()) {
        return stringList.get(stringList.size - 1)
    }
    return null
}

fun sanitizeUrltoPath(url: String): String {
    return url.replace("/", "-")
}

