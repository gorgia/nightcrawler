package nightcrawler.twitter.info

import nightcrawler.twitter.TwitterMailDifferenceBuilder
import nightcrawler.twitter.databases.mongo.TwitterTargetManager
import nightcrawler.twitter.databases.mongo.models.TwitterTarget
import nightcrawler.twitter.databases.neo4j.Neo4jTwitterFactory
import nightcrawler.twitter.databases.neo4j.models.TwitterAccount
import nightcrawler.twitter.databases.neo4j.models.TwitterStatus
import nightcrawler.twitter.utils.callAndWait
import nightcrawler.utils.log
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import twitter4j.*
import java.time.LocalDateTime
import java.util.*


/**
 * Created by andrea on 31/01/17.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class TwitterInfoGrabber(val tt: TwitterTarget) : Runnable {

    @Autowired
    lateinit var neo4jTwitterFactory: Neo4jTwitterFactory

    @Autowired
    lateinit var differenceMailBuilder: TwitterMailDifferenceBuilder

    @Autowired
    lateinit var twitterTargetManager: TwitterTargetManager

    @Autowired
    lateinit var ctx : ApplicationContext


    override fun run() {
        this.getInfo(this.tt)
    }

    fun getInfo(tt: TwitterTarget) {
        try {
            if (tt.isQuery) {
                this.getInfoQuery()
            } else this.getInfoAccount()
            twitterTargetManager.twitterTargetRepo.save(tt)
        } catch(e: Exception) {
            log().error("Exception during elaboration of twitterAccount identifier: ${tt.twitterIdentifier}", e)
            if (e.message?.contains("does not exists") ?: false) {
                log().error("could be that the user does not exists: forget of target for two weeks")
                twitterTargetManager.forgetForWeeks(tt)
            }
            if (e.message?.contains("rate limit") ?: false) {
                log().error("Rate limit reached. sleep for 15 minutes.")
                Thread.sleep(15 * 60 * 1000)
            }
        }
    }

    fun getInfoAccount() {
        log().info("Start processing twitter target: ${tt.twitterIdentifier}")
        val user = getUser(tt.twitterIdentifier!!)
        var oldTwitterAccount = neo4jTwitterFactory.getOrCreateNeo4jTwitterAccountFromTwitterUser(user)
        oldTwitterAccount = neo4jTwitterFactory.hidrateTwitterAccount(oldTwitterAccount)
        var newTwitterAccount: TwitterAccount? = null
        if (TwitterTargetManager.updateAttributesNecessary(tt)) {
            newTwitterAccount = this.updateAttributes(oldTwitterAccount)
            tt.lastUpdateAttributes = LocalDateTime.now()
        }
        if (newTwitterAccount == null) {
            log().error("TwitterAccount with twitterId:${tt.twitterIdentifier}")
        }
        var newStatuses: List<TwitterStatus> = ArrayList()
        if (TwitterTargetManager.updateTimeLineNecessary(tt)) {
            newStatuses = this.getNewStatuses(newTwitterAccount!!)
            tt.lastUpdateTimeline = LocalDateTime.now()
        }
        if (newTwitterAccount != null || newStatuses.isNotEmpty())
            differenceMailBuilder.buildAndSendDifferenceMail(tt, oldTwitterAccount = oldTwitterAccount, newTwitterAccount = newTwitterAccount, newStatuses = newStatuses)
    }

    fun getInfoQuery(query: String? = null): List<TwitterStatus> {
        log().info("Start processing twitter query: ${tt.twitterIdentifier}")
        val newStatuses: List<TwitterStatus> = this.getNewStatusesPerQuery(query ?: tt.twitterIdentifier!!)
        if (newStatuses.isNotEmpty() && tt.updateMailList.isNotEmpty()) {
            differenceMailBuilder.buildAndSendDifferenceMail(tt, oldTwitterAccount = null, newTwitterAccount = null, newStatuses = newStatuses)
        }
        tt.lastUpdateTimeline = LocalDateTime.now()
        return newStatuses
    }


    fun getNewStatusesPerQuery(queryString: String, saveInDb: Boolean = true): List<TwitterStatus> {
        val query = Query(queryString)
        val twitter = ctx.getBean(Twitter::class.java)
        val result = callAndWait { twitter.search(query) }
        val newTwitterStatuses = ArrayList<TwitterStatus>()
        result.tweets.forEach {
            val twitterStatus = neo4jTwitterFactory.getTwitterStatusFromStatus(it, lookInDb = true)
            if (twitterStatus.id == null) {
                if (saveInDb) newTwitterStatuses.add(neo4jTwitterFactory.twitterStatusRepository.save(twitterStatus))
                else newTwitterStatuses.add(twitterStatus)
            }
        }
        return newTwitterStatuses
    }

    fun getNewStatuses(twitterAccount: TwitterAccount, saveInDb: Boolean = true): List<TwitterStatus> {
        val twitter = ctx.getBean(Twitter::class.java)
        val statusList = callAndWait { twitter.getUserTimeline(twitterAccount.twitterId!!)!! }
        val newTwitterStatuses = ArrayList<TwitterStatus>()
        statusList.forEach {
            val twitterStatus = neo4jTwitterFactory.getTwitterStatusFromStatus(it, lookInDb = true)
            if (twitterStatus.id == null && saveInDb) {
                newTwitterStatuses.add(neo4jTwitterFactory.twitterStatusRepository.save(twitterStatus))
            }
        }
        return newTwitterStatuses
    }

    fun updateAttributes(twitterAccount: TwitterAccount): TwitterAccount {
        twitterAccount.followers.addAll(neo4jTwitterFactory.getNeo4jTwitterAccountsFromTwitterUsers(getFollowers(twitterAccount)))
        twitterAccount.followings.addAll(neo4jTwitterFactory.getNeo4jTwitterAccountsFromTwitterUsers(getFollowing(twitterAccount)))
        twitterAccount.likes.addAll(neo4jTwitterFactory.getNeo4jTwitterStatusesFromStatuses(getLikes(twitterAccount)))
        return twitterAccount
    }

    fun getUser(targetId: String): User {
        val twitter = ctx.getBean(Twitter::class.java)
        if (StringUtils.isNumeric(targetId))
            return callAndWait { twitter.showUser(targetId.toLong()) }
        else
            return callAndWait { twitter.showUser(targetId) }

    }

    fun getFollowing(twitterAccount: TwitterAccount): MutableSet<User> {
        val followingUserSet: MutableSet<User> = HashSet()
        var cursor: Long = -1
        var pagableFollowings: PagableResponseList<User>
        do {
            val twitter = ctx.getBean(Twitter::class.java)
            pagableFollowings = callAndWait { twitter.getFriendsList(twitterAccount.twitterId!!, cursor) }
            pagableFollowings.forEach { followingUserSet.add(it) }
            cursor = pagableFollowings.nextCursor
        } while (cursor != 0.toLong())
        return followingUserSet
    }

    fun getFollowers(twitterAccount: TwitterAccount): MutableSet<User> {
        val followersUserSet: MutableSet<User> = HashSet()
        var cursor: Long = -1
        var pagableFollowers: PagableResponseList<User>
        do {
            val twitter = ctx.getBean(Twitter::class.java)
            pagableFollowers = callAndWait { twitter.getFollowersList(twitterAccount.twitterId!!, cursor) }
            pagableFollowers.forEach { followersUserSet.add(it) }
            cursor = pagableFollowers.nextCursor
        } while (cursor != 0.toLong())
        return followersUserSet
    }

    fun getLikes(twitterAccount: TwitterAccount): MutableSet<Status> {
        val likesStatusSet: MutableSet<Status> = HashSet()
        var pageableLikes: ResponseList<Status>
        val paging = Paging()
        do {
            val twitter = ctx.getBean(Twitter::class.java)
            pageableLikes = callAndWait { twitter.getFavorites(twitterAccount.twitterId!!, paging) }
            pageableLikes.forEach { likesStatusSet.add(it) }
            if (paging.page <= 0) paging.page = 1
            paging.page = paging.page + 1
        } while (pageableLikes.isEmpty())
        return likesStatusSet
    }
}