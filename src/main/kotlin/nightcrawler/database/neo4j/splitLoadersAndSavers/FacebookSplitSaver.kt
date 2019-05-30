package nightcrawler.database.neo4j.splitLoadersAndSavers

import nightcrawler.database.neo4j.models.*
import nightcrawler.database.neo4j.repositories.FacebookAccountRepository
import nightcrawler.database.neo4j.repositories.FacebookBusinessPageRepository
import nightcrawler.database.neo4j.repositories.FacebookPageRepository
import nightcrawler.database.neo4j.repositories.FacebookPostRepository
import nightcrawler.utils.log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by andrea on 24/10/16.
 */
@Component
class FacebookSplitSaver {

    @Autowired
    lateinit var restApiNeo4jClient: RestApiNeo4jClient

    @Autowired
    lateinit var facebookPostRepository: FacebookPostRepository

    @Autowired
    lateinit var facebookPageRepository: FacebookPageRepository

    @Autowired
    lateinit var facebookAccountRepository: FacebookAccountRepository


    fun save(facebookEntity: FacebookEntity): FacebookEntity {
        entityInProcess = facebookEntity
        if (facebookEntity is FacebookPost) {
            entityInProcess = facebookPostRepository.save(facebookEntity)
        } else if (facebookEntity is FacebookPage) {
            entityInProcess = when (facebookEntity) {
                is FacebookAccount -> saveFacebookAccount(facebookEntity)
                is FacebookBusinessPage -> saveFacebookBusinessPage(facebookEntity)
                else -> saveFacebookPage(facebookEntity)
            }
        }
        return entityInProcess!!
    }

    var entityInProcess: FacebookEntity? = null

    fun saveFacebookBusinessPage(fbp: FacebookBusinessPage): FacebookBusinessPage {
        saveLikes(fbp)
        saveLikers(fbp)
        return fbp
    }

    fun saveFacebookAccount(fa: FacebookAccount): FacebookAccount {
        persistAllInDB(fa)
        saveFriends(fa)
        saveFollowers(fa)
        saveFollowing(fa)
        saveExperience(fa)
        saveLivesIn(fa)
        saveHasLivedIn(fa)
        saveLikes(fa)
        return fa
    }

    private fun persistAllInDB(fa: FacebookAccount) {
        if (fa.id == null) facebookPageRepository.save(fa, 0)
        fa.experience.forEach { facebookPageHasId(it) }
        fa.followers.forEach { facebookPageHasId(it) }
        fa.following.forEach { facebookPageHasId(it) }
        fa.friends.forEach { facebookPageHasId(it) }
        fa.hasLivedIn.forEach { facebookPageHasId(it) }
        fa.likes.forEach { facebookPageHasId(it) }
        if (fa.livesin != null) facebookPageHasId(fa.livesin!!)
    }

    private fun facebookPageHasId(fp: FacebookPage) {
        if (fp.id == null) facebookPageRepository.save(fp, 0)
    }

    @Throws(Exception::class)
    private fun saveLikes(fa: FacebookPage) {
        val likes = fa.likes
        if (likes.isEmpty()) return
        val params = HashMap<String, Any>()
        val likesIDS = ArrayList<Long>()
        likes.forEach { likeAccount -> likesIDS.add(likeAccount.id!!) }
        params.put("ids", likesIDS)
        params.put("id", fa.id!!)
        val query = "MATCH (n),(m) where id(n) = {id}\n" +
                " AND id(m) IN {ids}\n" +
                " CREATE UNIQUE (n)–[r:LIKES]->(m) return r"
        executeTransaction(query, params, "likes")
    }

    @Throws(Exception::class)
    private fun saveLikers(fbp: FacebookBusinessPage) {
        val likers = fbp.likers
        if (likers.isEmpty()) return
        val params = HashMap<String, Any>()
        val likersIDS = ArrayList<Long>()
        likers.forEach { likerAccount -> likersIDS.add(likerAccount.id!!) }
        params.put("ids", likersIDS)
        params.put("id", fbp.id!!)
        val query = "MATCH (n),(m) where id(n) = {id}\n" +
                " AND id(m) IN {ids}\n" +
                " CREATE UNIQUE (m)–[r:LIKES]->(n) return r"
        executeTransaction(query, params, "likers")
    }


    @Throws(Exception::class)
    private fun saveExperience(fa: FacebookAccount) {
        val experiences = fa.experience
        if (experiences.isEmpty()) return
        if (experiences.count() > 4) {
            log().warn("Error for ${fa.facebookName} experiences can't be > 4. I will not save them: please check.")
            return
        }
        val params = HashMap<String, Any>()
        val experiencesIDS = ArrayList<Long>()
        experiences.forEach { experience -> experiencesIDS.add(experience.id!!) }
        params.put("ids", experiencesIDS)
        params.put("id", fa.id!!)
        val query = "MATCH (n),(m) where id(n) = {id}\n" +
                "AND id(m) IN {ids}\n" +
                "CREATE UNIQUE (n)–[r:HAS_EXPERIENCE]->(m) return r"
        executeTransaction(query, params, "experience")
    }


    @Throws(Exception::class)
    private fun saveFriends(fa: FacebookAccount) {
        val friends = fa.friends
        if (friends.isEmpty()) return
        val params = HashMap<String, Any>()
        val friendsIDS = ArrayList<Long>()
        friends.forEach { friendAccount -> friendsIDS.add(friendAccount.id!!) }
        params.put("ids", friendsIDS)
        params.put("id", fa.id!!)
        val query = "MATCH (n),(m) where id(n) = {id}\n" +
                " AND id(m) IN {ids}\n" +
                " CREATE UNIQUE (n)–[r:HAS_AS_FRIEND]->(m) return r"
        executeTransaction(query, params, "friends")
    }

    @Throws(Exception::class)
    private fun saveFollowers(fa: FacebookAccount) {
        val followers = fa.followers
        if (followers.isEmpty()) return
        val params = HashMap<String, Any>()
        val followersIDS = ArrayList<Long>()
        followers.forEach { followerAccount -> followersIDS.add(followerAccount.id!!) }
        params.put("ids", followersIDS)
        params.put("id", fa.id!!)
        val query = "MATCH (n),(m) where id(n) = {id}\n" +
                " AND id(m) IN {ids}\n" +
                " CREATE UNIQUE (m)<–[r:IS_FOLLOWED_BY]-(n) return r"
        executeTransaction(query, params, "followers")
    }


    @Throws(Exception::class)
    private fun saveFollowing(fa: FacebookAccount) {
        val followings = fa.following
        if (followings.isEmpty()) return
        val params = HashMap<String, Any>()
        val followingIDS = ArrayList<Long>()
        followings.forEach { followingAccount -> followingIDS.add(followingAccount.id!!) }
        params.put("ids", followingIDS)
        params.put("id", fa.id!!)
        val query = "MATCH (n),(m) where id(n) = {id}\n" +
                "AND id(m) IN {ids}\n" +
                "CREATE UNIQUE (n)–[r:FOLLOWING]->(m) return r"
        executeTransaction(query, params, "following")
    }

    @Throws(Exception::class)
    private fun saveLivesIn(fa: FacebookAccount) {
        val livesin = fa.livesin
        if (livesin == null || livesin.id == null) return
        val params = HashMap<String, Any>()
        var previouslivesIn: FacebookPage? = null
        try {
            previouslivesIn = facebookAccountRepository.getLivesIn(fa.id!!)
        } catch(e: RuntimeException) {
            val query = "MATCH (n)–[r:LIVES_IN]->(m) where id(n) = {idparam} DELETE r"
            val params1 = HashMap<String, Any>()
            params1.put("idparam", fa.id!!)
            executeTransaction(query, params1, "livesin")
        }
        if (previouslivesIn != null && previouslivesIn.id == livesin.id) return
        else if (previouslivesIn != null && previouslivesIn.id != livesin.id) {
            fa.hasLivedIn.add(previouslivesIn)
            params.put("id1", fa.id!!)
            params.put("id2", previouslivesIn.id!!)
            val query = "MATCH (n)–[r:LIVES_IN]->(m) where id(n) = {id1}\n" +
                    "AND id(m) = {id2}\n" +
                    "DELETE r"
            executeTransaction(query, params, "livesin")
        }
        params.put("id1", fa.id!!)
        params.put("id2", livesin.id!!)
        val query = "MATCH (n),(m) where id(n) = {id1}\n" +
                "AND id(m) = {id2}\n" +
                "CREATE UNIQUE (n)–[r:LIVES_IN]->(m) return r"
        executeTransaction(query, params, "livesin")
    }

    @Throws(Exception::class)
    private fun saveHasLivedIn(fa: FacebookAccount) {
        val haslivedin = fa.hasLivedIn
        if (haslivedin.isEmpty()) return
        if (haslivedin.count() > 4) {
            log().warn("Error for ${fa.facebookName} hasLivedIn can't be > 4. I will not save them: please check.")
            return
        }
        val params = HashMap<String, Any>()
        val haslivedinIDS = ArrayList<Long>()
        haslivedin.forEach { haslivein -> haslivedinIDS.add(haslivein.id!!) }
        params.put("ids", haslivedinIDS)
        params.put("id", fa.id!!)
        val query = "MATCH (n),(m) where id(n) = {id}\n" +
                "and id(m) IN {ids}\n" +
                "CREATE UNIQUE (n)–[r:HAS_LIVED_IN]->(m) return r"
        executeTransaction(query, params, "haslivedin")
    }

    private fun executeTransaction(cypherQuery: String, params: Map<String, Any>, pointOfSaving: String) {
        try {
            restApiNeo4jClient.sendCypher(cypherQuery, params)
        } catch (e: Exception) {
            log().error("Problems during saving of " + pointOfSaving + " for entity " + entityInProcess!!.facebookId, e)
        }
    }

    fun saveFacebookPage(facebookPage: FacebookPage): FacebookPage {
        return facebookPageRepository.save(facebookPage)
    }

    fun savePost(facebookPost: FacebookPost): FacebookPost {
        return facebookPostRepository.save(facebookPost)
    }
}