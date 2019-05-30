package nightcrawler.twitter.utils

import nightcrawler.twitter.databases.mongo.models.TwitterLoginAccount
import nightcrawler.twitter.databases.mongo.repo.TwitterLoginAccountRepository
import nightcrawler.twitter.webcrawler.login.TwitterLoginAccountManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.lang.reflect.Method
import java.util.*
import javax.annotation.PostConstruct

/**
 * Created by andrea on 31/01/17.
 */
@Component
open class MyTwitterFactory {

    @Autowired
    lateinit var twitterLoginAccountManager: TwitterLoginAccountManager

    /*
    var twitterInstance: Twitter? = null

    fun getTwitter(twitterIdentifier: String? = null): Twitter {
        if (twitterInstance != null) return twitterInstance!!
        val loginAccount: TwitterLoginAccount = if (twitterIdentifier != null) twitterLoginAccountManager.twitterLoginAccountRepo.findOne(twitterIdentifier) else twitterLoginAccountManager.getRandomLoginAccount()
        val cb = ConfigurationBuilder()
                .setDebugEnabled(true)
                .setOAuthConsumerKey(loginAccount.consumerKey)
                .setOAuthConsumerSecret(loginAccount.consumerSecret)
                .setOAuthAccessToken(loginAccount.accessToken)
                .setOAuthAccessTokenSecret(loginAccount.accessTokenSecret)
        val tf = TwitterFactory(cb.build())
        this.twitterInstance = tf.instance
        return twitterInstance!!
    }*/

    fun getTwitter(twitterIdentifier: String? = null): Twitter {
        val loginAccount: TwitterLoginAccount = if (twitterIdentifier != null) twitterLoginAccountManager.twitterLoginAccountRepo.findById(twitterIdentifier).get() else(twitterLoginAccountManager.getRandomLoginAccount())
        //val loginAccount: TwitterLoginAccount = if (twitterIdentifier != null) twitterLoginAccountManager.twitterLoginAccountRepo.findOne(twitterIdentifier) else(twitterLoginAccountManager.getRandomLoginAccount())
        val cb = ConfigurationBuilder()
                .setDebugEnabled(true)
                .setOAuthConsumerKey(loginAccount.consumerKey)
                .setOAuthConsumerSecret(loginAccount.consumerSecret)
                .setOAuthAccessToken(loginAccount.accessToken)
                .setOAuthAccessTokenSecret(loginAccount.accessTokenSecret)
        val tf = TwitterFactory(cb.build())
        return tf.instance
    }


}