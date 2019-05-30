package nightcrawler.twitter.databases.neo4j

import com.google.common.collect.Lists
import nightcrawler.twitter.databases.neo4j.models.TwitterAccount
import nightcrawler.twitter.databases.neo4j.models.TwitterStatus
import nightcrawler.twitter.databases.neo4j.repositories.TwitterAccountRepository
import nightcrawler.twitter.databases.neo4j.repositories.TwitterStatusRepository
import nightcrawler.twitter.utils.callAndWait
import nightcrawler.twitter.utils.dateToLocalDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import twitter4j.Status
import twitter4j.Twitter
import twitter4j.User
import java.util.*

/**
 * Created by andrea on 31/01/17.
 */


@Component
open class Neo4jTwitterFactory {

    @Autowired
    lateinit var twitterAccountRepository: TwitterAccountRepository

    @Autowired
    lateinit var twitterStatusRepository: TwitterStatusRepository

    @Autowired
    lateinit var ctx: ApplicationContext

    open fun getNeo4jTwitterAccountFromTwitterUserId(userId: Long): TwitterAccount? {
        if (userId <= 0) return null
        val ta = twitterAccountRepository.findByTwitterId(userId)
        if (ta != null) return ta
        val twitter = ctx.getBean(Twitter::class.java)
        val user = callAndWait { twitter.showUser(userId) }
        return getOrCreateNeo4jTwitterAccountFromTwitterUser(user)
    }

    open fun getOrCreateNeo4jTwitterAccountFromTwitterUser(twitterUser: User, saveInNeo4j: Boolean = false): TwitterAccount {
        var ta = twitterAccountRepository.findByTwitterId(twitterUser.id)
        if (ta != null) return ta
        ta = TwitterAccount()
        ta.name = twitterUser.name
        ta.twitterId = twitterUser.id
        ta.profilePic = twitterUser.originalProfileImageURL
        ta.createdAt = dateToLocalDateTime(twitterUser.createdAt)
        ta.description = twitterUser.description
        ta.email = twitterUser.email
        ta.isGeoEnabled = twitterUser.isGeoEnabled
        ta.isProtected = twitterUser.isProtected
        ta.lang = twitterUser.lang
        ta.location = twitterUser.location
        if (saveInNeo4j) ta = twitterAccountRepository.save(ta)
        return ta!!
    }

    open fun getNeo4jTwitterAccountsFromTwitterUsers(twitterUsers: Set<User>, saveInNeo4j: Boolean = false): Set<TwitterAccount> {
        val twitterAccountsSaved: MutableSet<TwitterAccount> = HashSet()
        val twitterAccountToBeSaved: MutableSet<TwitterAccount> = HashSet()
        twitterUsers.forEach {
            val ta = getOrCreateNeo4jTwitterAccountFromTwitterUser(it, false)
            if (ta.id != null) {
                twitterAccountsSaved.add(ta)
            } else twitterAccountToBeSaved.add(ta)
        }
        twitterAccountsSaved.addAll(twitterAccountRepository.saveAll(twitterAccountToBeSaved))
        //twitterAccountsSaved.addAll(twitterAccountRepository.save(twitterAccountToBeSaved))
        return twitterAccountsSaved
    }


    open fun getNeo4jTwitterAccountsFromTwitterUsersIds(twitterUsers: LongArray, saveInNeo4j: Boolean = false): Set<TwitterAccount> {
        if (twitterUsers.isEmpty()) return HashSet()
        val twitterAccountsSaved: MutableSet<TwitterAccount> = HashSet()
        val usersToLookUp = HashSet<Long>()
        twitterUsers.forEach {
            val ta = twitterAccountRepository.findByTwitterId(it)
            if (ta != null) twitterAccountsSaved.add(ta)
            usersToLookUp.add(it)
        }
        Lists.partition(usersToLookUp.toList(), 100).forEach {
            val twitter = ctx.getBean(Twitter::class.java)
            twitterAccountsSaved.addAll(getNeo4jTwitterAccountsFromTwitterUsers(callAndWait { twitter.lookupUsers(*twitterUsers)}.toSet(), saveInNeo4j = saveInNeo4j))
        }
        return twitterAccountsSaved
    }

    open fun hidrateTwitterAccount(twitterAccount: TwitterAccount): TwitterAccount {
        twitterAccount.followers.addAll(twitterAccountRepository.getFollowers(twitterAccount.twitterId!!))
        twitterAccount.followings.addAll(twitterAccountRepository.getFollowing(twitterAccount.twitterId!!))
        twitterAccount.likes.addAll(twitterAccountRepository.getLikes(twitterAccount.twitterId!!))
        return twitterAccount
    }


    open fun getTwitterStatusFromStatus(statusId: Long): TwitterStatus? {
        if (statusId <= 0) return null
        val twitter = ctx.getBean(Twitter::class.java)
        return getTwitterStatusFromStatus(callAndWait { twitter.showStatus(statusId) as Status })
    }

    open fun getTwitterStatusFromStatus(status: Status, lookInDb: Boolean = true): TwitterStatus {
        var twitterStatus: TwitterStatus? = null
        if (lookInDb) {
            twitterStatus = twitterStatusRepository.findByTwitterId(status.id)
        }
        if (twitterStatus == null)
            twitterStatus = TwitterStatus()
        twitterStatus.author = this.getOrCreateNeo4jTwitterAccountFromTwitterUser(status.user, saveInNeo4j = true)
        twitterStatus.author?.doesExists = true
        twitterStatus.createdAt = dateToLocalDateTime(status.createdAt)
        twitterStatus.twitterId = status.id
        twitterStatus.text = status.text
        if (status.quotedStatus != null) twitterStatus.quotedStatus = getTwitterStatusFromStatus(status.quotedStatus)
        if (status.contributors.isNotEmpty()) twitterStatus.contributors = this.getNeo4jTwitterAccountsFromTwitterUsersIds(status.contributors)
        if (status.inReplyToStatusId > 0) twitterStatus.inReplyToStaus = getTwitterStatusFromStatus(status.inReplyToStatusId)
        if (status.inReplyToUserId > 0) twitterStatus.inReplyToUser = getNeo4jTwitterAccountFromTwitterUserId(status.inReplyToUserId)
        return twitterStatus
    }

    open fun getNeo4jTwitterStatusesFromStatuses(statuses: Collection<Status>): List<TwitterStatus> {
        val statusesIds = statuses.map { it.id }
        val twitterStatusesAlreadyInDB = twitterStatusRepository.findByTwitterIdIn(statusesIds)
        val statusesIdsAlreadyInDb = twitterStatusesAlreadyInDB.map { it.twitterId }
        val statusesIdsNotInDb = statusesIds.filter { !statusesIdsAlreadyInDb.contains(it) }
        val statusesNotInDb = statuses.filter { statusesIdsNotInDb.contains(it.id) }
        val returnList: MutableList<TwitterStatus> = ArrayList()
        statusesNotInDb.forEach { returnList.add(getTwitterStatusFromStatus(it, false)) }
        returnList.addAll(twitterStatusesAlreadyInDB)
        return returnList
    }


}
