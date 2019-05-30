package nightcrawler.facebook.differencemonitor

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import nightcrawler.database.mongodb.models.FacebookTargetMongo
import nightcrawler.database.neo4j.models.FacebookAccount
import nightcrawler.database.neo4j.models.FacebookEntity
import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.database.neo4j.models.FacebookPost
import nightcrawler.mail.MailSender
import nightcrawler.utils.log
import java.util.*
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart

/**
 * Created by andrea on 17/08/16.
 */
object DifferenceMailBuilder {


    fun buildAndSendDifferenceMail(ft: FacebookTargetMongo, oldFa: FacebookPage, newFa: FacebookPage, newPosts: Set<FacebookPost>) {
        if (ft.updateMailList == null || ft.updateMailList!!.isEmpty()) {
            log().debug("There is no need to compute differences since there are no recipients for a mail update")
            return
        }
        val newFeatures: Multimap<String, FacebookEntity> = getDifferences(oldFa, newFa)
        sendComplexMail(ft.updateMailList!!, newFa, newPosts, newFeatures)
    }

    fun sendComplexMail(recipients: Set<String>, fa: FacebookPage, newposts: Set<FacebookPost>, newfeatures: Multimap<String, FacebookEntity>) {
        if (newposts.isEmpty() && newfeatures.isEmpty) {
            log().info("No update mail needed. There are no updates for account ${fa.facebookName}")
            return
        }
        val subject = "news from facebook of " + fa.facebookName

        //MULTIPART MAIL BODY CREATION
        // This mail has 2 part, the BODY and the embedded image
        val multipart = MimeMultipart("related")

        // first part (the html)
        val mimeBodyPart = MimeBodyPart()

        var htmlText = ""

        if (!newposts.isEmpty()) {
            htmlText += "<H2>New Posts:</H2><br>"
            for (newpost in newposts) {
                htmlText = if (newpost.images?.isNotEmpty() == true) {
                    htmlText + "Url:" + newpost.url + "<br>Type: " + newpost.facebookPostType + "<br><img src=\"${newpost.images?.first()}\"/>" + "<br>Date: " + newpost.date + "<br>Text:<br>" + if (!newpost.text.isNullOrBlank()) newpost.text else ""
                } else
                    htmlText + "Url:" + newpost.url + "<br>Type: " + newpost.facebookPostType + "<br>Date: " + newpost.date + "<br>Text:<br>" + if (!newpost.text.isNullOrBlank()) newpost.text else ""
            }
        }

        if (!newfeatures.isEmpty) {
            htmlText += "<br><br><br>New Elements: <br>"
            for (s in newfeatures.keySet()) {
                htmlText = htmlText + "New elements of type " + s
                for (facebookEntity in newfeatures.get(s)) {
                    if (facebookEntity is FacebookPage) {
                        htmlText += "Facebook Account: <br>name: ${facebookEntity.facebookName} userID: ${facebookEntity.facebookId} <br>"
                    } else {
                        htmlText = htmlText + "Facebook Entity text: <br>" + facebookEntity.toString() + "<br>"
                    }
                }
                htmlText += "<br><br>"
            }
        }
        log().info("Sending mail to $recipients with text: \n$htmlText ")
        mimeBodyPart.setContent(htmlText, "text/html; charset=UTF-8")
        multipart.addBodyPart(mimeBodyPart)
        val cc = setOf("gorgia@fastwebnet.it")
        MailSender.sendMail(recipients, cc, "socialnet@octopus.org", subject, multipart)
    }

    private fun getDifferences(oldFa: FacebookPage, newFa: FacebookPage): Multimap<String, FacebookEntity> {
        val newfeatures = HashMultimap.create<String, FacebookEntity>()
        if (oldFa is FacebookAccount && newFa is FacebookAccount) {
            val newFriends = checkDifferencesBetweenFacebookLists(oldFa.friends, newFa.friends)
            val newFollowings = checkDifferencesBetweenFacebookLists(oldFa.following, newFa.following)
            val newFollowers = checkDifferencesBetweenFacebookLists(oldFa.followers, newFa.following)
            val newExperience = checkDifferencesBetweenFacebookLists(oldFa.experience, newFa.experience)
            val newHasLivedIn = checkDifferencesBetweenFacebookLists(oldFa.hasLivedIn, newFa.hasLivedIn)
            newfeatures.putAll("friends", newFriends)
            newfeatures.putAll("followers", newFollowers)
            newfeatures.putAll("followings", newFollowings)
            newfeatures.putAll("experience", newExperience)
            newfeatures.putAll("haslivedin", newHasLivedIn)
        }
        return newfeatures
    }


    private fun <T : FacebookEntity> checkDifferencesBetweenFacebookLists(oldColletion: Collection<T>?, newCollection: Collection<T>?): List<T> {
        if (oldColletion == null || oldColletion.isEmpty() || newCollection == null || newCollection.isEmpty()) {
            return emptyList()
        }
        val oldCollectionFacebookIds = HashSet<String?>()
        oldColletion.forEach { oldCollectionFacebookIds.add(it.facebookId) }
        val newElements = newCollection.filter { !oldCollectionFacebookIds.contains(it.facebookId) }
        return newElements
    }


}