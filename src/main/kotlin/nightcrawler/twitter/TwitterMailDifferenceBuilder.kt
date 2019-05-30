package nightcrawler.twitter

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import nightcrawler.mail.MailSender
import nightcrawler.twitter.databases.mongo.models.TwitterTarget
import nightcrawler.twitter.databases.neo4j.models.TwitterAccount
import nightcrawler.twitter.databases.neo4j.models.TwitterEntity
import nightcrawler.twitter.databases.neo4j.models.TwitterStatus
import nightcrawler.utils.log
import org.springframework.stereotype.Component
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart

/**
 * Created by andrea on 10/02/17.
 */
@Component
class TwitterMailDifferenceBuilder {

    fun buildAndSendDifferenceMail(tt: TwitterTarget, oldTwitterAccount: TwitterAccount?, newTwitterAccount: TwitterAccount?, newStatuses: Collection<TwitterStatus>) {
        if (tt.updateMailList.isEmpty()) {
            log().debug("There is no need to compute differences since there are no recipients for a mail update")
            return
        }
        var newFeatures: Multimap<String, TwitterEntity> = HashMultimap.create()
        if (oldTwitterAccount != null && newTwitterAccount != null)
            newFeatures = this.getDifferences(oldTwitterAccount, newTwitterAccount)
        this.sendComplexMail(tt.updateMailList!!, newTwitterAccount?.name ?: tt.twitterIdentifier!!, newStatuses, newFeatures)
    }


    private fun getDifferences(oldTwitterAccount: TwitterAccount, newTwitterAccount: TwitterAccount): Multimap<String, TwitterEntity> {
        val newfeatures = HashMultimap.create<String, TwitterEntity>()
        val newFollowers = this.checkDifferencesBetweenTwitterLists(oldTwitterAccount.followers, newTwitterAccount.followers)
        val newFollowings = this.checkDifferencesBetweenTwitterLists(oldTwitterAccount.followings, newTwitterAccount.followings)
        val newLikes = this.checkDifferencesBetweenTwitterLists(oldTwitterAccount.likes, newTwitterAccount.likes)
        newfeatures.putAll("followers", newFollowers)
        newfeatures.putAll("followings", newFollowings)
        newfeatures.putAll("likes", newLikes)

        return newfeatures
    }


    private fun <T : TwitterEntity> checkDifferencesBetweenTwitterLists(oldColletion: Collection<T>?, newCollection: Collection<T>?): List<T> {
        if (oldColletion == null || oldColletion.isEmpty() || newCollection == null || newCollection.isEmpty()) {
            return emptyList()
        }
        val oldCollectionFacebookIds = java.util.HashSet<Long?>()
        oldColletion.forEach { oldCollectionFacebookIds.add(it.twitterId) }
        val newElements = newCollection.filter { !oldCollectionFacebookIds.contains(it.twitterId) }
        return newElements
    }

    private fun sendComplexMail(updateMailList: Set<String>, twitterIdentifier: String, newStatuses: Collection<TwitterStatus>, newFeatures: Multimap<String, TwitterEntity>) {
        if (newStatuses.isEmpty() && newFeatures.isEmpty) {
            log().info("No update mail needed. There are no updates for account $twitterIdentifier")
            return
        }
        val subject = "news from twitter of $twitterIdentifier"

        //MULTIPART MAIL BODY CREATION
        // This mail has 2 part, the BODY and the embedded image
        val multipart = MimeMultipart("related")

        // first part (the html)
        val mimeBodyPart = MimeBodyPart()

        var htmlText = ""

        if (!newStatuses.isEmpty()) {
            htmlText += "<H2>New Tweets:</H2><br>"
            for (newStatus in newStatuses) {
                htmlText = htmlText + "Url:" + "http://twitter/${newStatus.twitterId}" + "<br>Date: " + newStatus.createdAt + "<br>Text:<br>" + if (!newStatus.text.isNullOrBlank()) newStatus.text else ""
            }
        }

        if (!newFeatures.isEmpty) {
            htmlText += "<br><br><br>New Elements: <br>"
            for (s in newFeatures.keySet()) {
                htmlText = htmlText + "New elements of type " + s
                for (twitterEntity in newFeatures.get(s)) {
                    if (twitterEntity is TwitterAccount) {
                        htmlText += "Twitter Account: <br>name: ${twitterEntity.name} userID: ${twitterEntity.twitterId} <br>"
                    } else {
                        htmlText = htmlText + "Facebook Entity text: <br>" + twitterEntity.toString() + "<br>"
                    }
                }
                htmlText += "<br><br>"
            }
        }
        log().info("Sending mail to $updateMailList with text: \n$htmlText ")
        mimeBodyPart.setContent(htmlText, "text/html; charset=UTF-8")
        multipart.addBodyPart(mimeBodyPart)
        val cc = setOf("gorgia@fastwebnet.it")
        MailSender.sendMail(updateMailList, cc, "socialnet@octopus.org", subject, multipart)
    }
}