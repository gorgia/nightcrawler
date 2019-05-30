package nightcrawler.facebook.info.infograbber.timeline

import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.database.neo4j.models.FacebookPost
import nightcrawler.database.neo4j.service.FacebookPostType
import nightcrawler.facebook.info.FacebookNeo4jEntityFactory
import nightcrawler.utils.log
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*


@Component
class PostCommentGrabber {

    @Autowired
    lateinit var facebookNeo4jEntityFactory: FacebookNeo4jEntityFactory

    fun extractData(postWrapper: Element, eagerComments: Boolean) : MutableSet<FacebookPost>{
        val commentSet: MutableSet<FacebookPost> = HashSet()
        val commentsWrappers: Elements = postWrapper.select("UFICommentContent")
        commentsWrappers.reversed().forEach {
            val commentFacebookId = getCommentFacebookId(it)
            if(commentFacebookId == null) {
                log().warn("Impossibile to retrieve facebookId for element: \n$it")
                return commentSet
            }
            val comment = facebookNeo4jEntityFactory.getByFacebookId(commentFacebookId, FacebookPost::class.java) ?: FacebookPost()
            if (comment.id != null && !eagerComments) return commentSet
            comment.facebookId = commentFacebookId
            comment.author = getAuthor(it)
            comment.text = getTextBody(it)
            comment.date = getDate(it)
            comment.facebookPostType = FacebookPostType.COMMENT
            comment.images = getImages(it)
            commentSet.add(comment)
        }
        return commentSet
    }


    private fun getCommentFacebookId (commentWrapper : Element) : String? {
        val uiLink = commentWrapper.getElementsByClass("uiLinkSubtle").first() ?: return null
        return uiLink.attr("data-reactid").split("comment_id=").last().split("&")[0]
    }

    private fun getTextBody(commentWrapper : Element): String?{
        return commentWrapper.getElementsByClass("UFICommentBody").text()
    }

    private fun getAuthor (commentWrapper: Element): FacebookPage?{
        val commentAuthorElement = commentWrapper.getElementsByClass("UFICommentActorName").first() ?: return null
        return this.facebookNeo4jEntityFactory.getOrCreateFacebookPageFromElement(commentAuthorElement, FacebookPage::class.java)
    }

    private fun getDate(commentWrapper: Element) : Date?{
        val uilink = commentWrapper.getElementsByClass("uiLinkSubtle").first() ?: return null
        val dataUtime = uilink.attr("data-utime")
        return Date(dataUtime.toLong() * 1000)
    }

    private fun getImages(commentWrapper: Element) : HashSet<String>{
        val imagesel = commentWrapper.getElementsByClass("mvs")
        val images = imagesel
                .map { it.getElementsByTag("img") }
                .filterNot { it.isEmpty() }
                .map { it[0] }
                .mapNotNullTo(HashSet<String>()) { it.attr("src") }
        return images
    }


}