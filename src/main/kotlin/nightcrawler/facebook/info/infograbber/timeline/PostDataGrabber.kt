package nightcrawler.facebook.info.infograbber.timeline

import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.database.neo4j.models.FacebookPost
import nightcrawler.database.neo4j.service.FacebookPostType
import nightcrawler.facebook.info.FacebookNeo4jEntityFactory
import nightcrawler.facebook.info.fromUtimeStringToDate
import nightcrawler.springconfiguration.CamelConfig
import nightcrawler.utils.log
import org.apache.camel.EndpointInject
import org.apache.camel.ProducerTemplate
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.stereotype.Component
import org.springframework.test.context.ContextConfiguration
import java.util.*

/**
 * Created by andrea on 09/08/16.
 */

@Component
@ContextConfiguration(classes = arrayOf(CamelConfig::class))
class PostDataGrabber {
    @Autowired
    @Qualifier("facebookPostRepository")
    lateinit var neo4jRepo: Neo4jRepository<FacebookPost, Long>

    @Autowired
    lateinit var commentGrabber: PostCommentGrabber


    @Autowired
    lateinit var facebookNeo4jEntityFactory: FacebookNeo4jEntityFactory

    @EndpointInject(uri = "jms:queue:likesUrl?timeToLive=3000000")
    internal lateinit var likesUrlProducer: ProducerTemplate

    @EndpointInject(uri = "jms:queue:sharesUrl?timeToLive=3000000")
    internal lateinit var sharesUrlProducer: ProducerTemplate

    fun extractData(facebookId: String, element: Element, eagerComments: Boolean = false): FacebookPost {
            val postAlreadyInDb = facebookNeo4jEntityFactory.getByFacebookId(facebookId, FacebookPost::class.java, completeLoad = true)
            if (postAlreadyInDb != null && !eagerComments) return postAlreadyInDb
            var facebookPost = postAlreadyInDb ?: FacebookPost()
            facebookPost.facebookId = facebookId
            facebookPost.url = getUrl(element)
            log().info("Extracting post data from post at url: ${facebookPost.url}")
            facebookPost.facebookPostType = getPostType(facebookPost.url!!)
            facebookPost.author = getAuthor(element) ?: throw Exception("Author not found in subElement:\n $element")
            facebookPost.date = getDate(element) ?: throw Exception("Date not found in subElement:\n $element")
            facebookPost.text = getContent(element)
            facebookPost.images = getImageInPost(element)
            facebookPost.comments = commentGrabber.extractData(element, eagerComments)
            facebookPost = neo4jRepo.save(facebookPost, -1)
            sendToLikesExtractor(element, facebookPost)
            sendToSharesExtractor(element, facebookPost)
        return facebookPost
    }

    private fun getUrl(postWrapper: Element): String {
        val urlElements = postWrapper.getElementsByClass("_5pcq")
        var postURL = urlElements[0].attr("href")!!
        if (!postURL.contains("https://www.facebook.com")) {
            postURL = "https://www.facebook.com$postURL"
        }
        return postURL
    }


    private fun getPostType(postUrl: String): FacebookPostType {
        if (postUrl.contains("video.php"))
            return FacebookPostType.VIDEO
        else if (postUrl.contains("posts"))
            return FacebookPostType.POST
        else if (postUrl.contains("photo"))
            return FacebookPostType.PHOTO
        else if (postUrl.contains("notes"))
            return FacebookPostType.NOTE
        return FacebookPostType.POST
    }

    private fun sendToLikesExtractor(postWrapper: Element, facebookPost: FacebookPost): Set<FacebookPage> {
        val likeAccounts = HashSet<FacebookPage>()
        try {
            val likeUrlElements = postWrapper.getElementsByClass("UFILikeSentence")

            if (likeUrlElements.isNotEmpty()) {
                val ufiLikesSentence = likeUrlElements.first()
                val x4vs = ufiLikesSentence.getElementsByAttributeValueStarting("href", "/ufi/reaction/profile/browser/?ft_ent_identifier")
                if (x4vs.isNotEmpty())
                    likesUrlProducer.sendBodyAndHeader(facebookPost, "likesUrl", "https://facebook.com${x4vs.first().attr("href")}")
            }
        } catch (e: Exception) {
            log().error("Get Likes Failed", e)
        }
        return likeAccounts
    }


    private fun sendToSharesExtractor(postWrapper: Element, facebookPost: FacebookPost): MutableSet<FacebookPost> {
        val shareLinks = postWrapper.getElementsByClass("UFIShareLink")
        if (shareLinks.isNotEmpty()) {
            sharesUrlProducer.sendBody(facebookPost)
        }
        return HashSet()
    }


    private fun getAuthor(postWrapper: Element): FacebookPage? {
        val authorsElementsDataHovercard = postWrapper.getElementsByAttributeValueStarting("data-hovercard", "/ajax/hovercard")
        if (authorsElementsDataHovercard.isNotEmpty() && authorsElementsDataHovercard[0] != null)
            return facebookNeo4jEntityFactory.getOrCreateFacebookPageFromElement(authorsElementsDataHovercard[0], FacebookPage::class.java)!!
        //postWrapper.getElementsByAttributeValueStarting("data-hovercard", "/ajax/hovercard")
        val longUrlElement = postWrapper.getElementsByAttribute("axify").first() ?: return null
        val axify = longUrlElement.attr("axify")
        val facebookId = extractFacebookIdFromAxify(axify)
        if (facebookId != null) {
            val facebookAccount = facebookNeo4jEntityFactory.getByFacebookId(facebookId, FacebookPage::class.java)!!
            return facebookAccount
        }
        return null
    }

    private fun extractFacebookIdFromAxify(axify: String): String? {
        try {
            var axifySplit = axify.split("set=")[1]
            axifySplit = axifySplit.split("&")[0]
            val splitArray = axifySplit.split(".")
            return splitArray[splitArray.size - 1]
        } catch (e: Exception) {
            log().error("Axify split exception", e)
        }
        return null
    }

    /**
     * grab post date
     */
    private fun getDate(postWrapper: Element): Date? {
        val webdatas = postWrapper.getElementsByAttribute("data-utime")
        if (webdatas.size > 0) {
            val utimeString = webdatas.first().attr("data-utime")
            return fromUtimeStringToDate(utimeString) //on facebook dates are in UTime
        }
        return null
    }

    private fun getContent(postWrapper: Element): String? {
        val ps = postWrapper.getElementsByTag("p")
        var text: String? = null
        if (ps.isNotEmpty()) {
            text = ps[0]?.text()
        }
        return text
    }


    protected fun getImageInPost(postDoc: Element): MutableSet<String> {
        val images = HashSet<String>()
        try {
            val imageElement = postDoc.getElementById("imagestage")
            if (imageElement != null) {
                val photoel = imageElement.getElementById("fbPhotoImage")
                if (photoel == null) {
                    if (imageElement.getElementsByClass("videoStage") != null) {
                        log().info("Url: contains a video. Not all videos are implemented yet")
                        return images
                    }
                    return images
                }
                val imgSource = photoel.attr("src")
                images.add(imgSource)
            } else {
                val uiScaledImageContainer = postDoc.getElementsByClass("scaledImageFitWidth").first()
                if (uiScaledImageContainer != null) images.add(uiScaledImageContainer.attr("src"))
            }
        } catch (e: Exception) {
            log().error("Exception during image extraction", e)

        }
        return images
    }


    protected fun getComments(postWrapper: Element, eagerComments: Boolean): MutableSet<FacebookPost> {
        val commentSet: MutableSet<FacebookPost> = HashSet()
        val commentsWrappers: Elements = postWrapper.select("UFICommentContent")
        commentsWrappers.forEach({ webcomment ->
            var uilinkhref: String? = null
            try {
                val uilinks = webcomment.getElementsByClass("uiLinkSubtle")
                if (uilinks.isNotEmpty()) {
                    uilinkhref = uilinks[0].attr("href")
                    val reactid = uilinks[0].attr("data-reactid")
                    val facebookId: String? = reactid.split("comment_id=").last().split("&")[0]
                    var comment = facebookNeo4jEntityFactory.getByFacebookId(facebookId!!, FacebookPost::class.java) ?: FacebookPost()
                    if (comment.id != null && !eagerComments) return commentSet
                    if (comment.facebookId == null || eagerComments) {
                        comment.facebookId = facebookId
                        comment.url = uilinkhref

                        //getDate
                        val dataUtimeList = uilinks[0].getElementsByAttribute("data-utime")
                        var commentDate: Date? = null
                        if (dataUtimeList != null && dataUtimeList.size > 0) {
                            commentDate = Date(java.lang.Long.parseLong(dataUtimeList[0].attr("data-utime")) * 1000)
                        }
                        comment.date = commentDate

                        val commentbodies = webcomment.getElementsByClass("UFICommentBody")
                        //get text of comment
                        if (commentbodies.size > 0) {
                            comment.text = commentbodies[0].attr("textContent")
                        }
                        comment.facebookPostType = FacebookPostType.COMMENT

                        //grab comment author details
                        val webauthors = webcomment.getElementsByClass("UFICommentActorName")
                        if (webauthors.isNotEmpty()) {
                            val webauthor = webauthors[0]
                            val author = this.facebookNeo4jEntityFactory.getOrCreateFacebookPageFromElement(webauthor, FacebookPage::class.java)
                            comment.author = author
                        }
                        //extract images of comment
                        val imagesel = webcomment.getElementsByClass("mvs")
                        val images = HashSet<String>()
                        for (imageel in imagesel) {
                            val imgs = imageel.getElementsByTag("img")
                            if (!imgs.isEmpty()) {
                                val photoel = imgs[0]
                                val imgSource = photoel.attr("src")
                                if (imgSource != null) {
                                    images.add(imgSource)
                                }
                            }
                        }
                        comment.images = images
                        //saveFacebookAccount comment
                        comment = neo4jRepo.save(comment, 1)
                        commentSet.add(comment)
                    }
                }
            } catch (e: Exception) {
                log().error("Error during extraction of comments: $")
            }
        })
        return commentSet
    }

}