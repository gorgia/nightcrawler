package nightcrawler.twitter.databases.mongo.models

import nightcrawler.TestConfiguration
import nightcrawler.twitter.databases.mongo.repo.TwitterLoginAccountRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

/**
 * Created by andrea on 31/01/17.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = arrayOf("scheduling.enabled=false"), classes = arrayOf(TestConfiguration::class))
open class TwitterLoginAccountTest{
    @Autowired
    lateinit var twitterLoginAccountRepo : TwitterLoginAccountRepository

    val twitterIdentifier = "blablablanche"
    val consumerKey = "SCtWaGDOGWlTjVv65ZHISIPpk"
    val consumerSecret = "r0TJdyGoNJI3ylRclkCNAZd1FGuXHaMHRlXJMFWh7HdwusmC4y"
    val accessToken = "819885189191049217-ATQbrGBEImw8kFUu6gfrFwZU3xDnrHp"
    val accessTokenSecret = "qaOaDAWqZwNgvqXFObddGkqVQfa3cBWMlanEYSJm4LLID"


    @Ignore
    @Test
    fun saveTest(){
        var loginAccount = twitterLoginAccountRepo.findById(twitterIdentifier).orElse(null)
        //var loginAccount = twitterLoginAccountRepo.findOne(twitterIdentifier)
        if(loginAccount == null){
            loginAccount = TwitterLoginAccount()
            loginAccount.twitterIdentifier = twitterIdentifier
            loginAccount.consumerKey = consumerKey
            loginAccount.consumerSecret = consumerSecret
            loginAccount.accessToken = accessToken
            loginAccount.accessTokenSecret = accessTokenSecret
        }
        loginAccount = twitterLoginAccountRepo.save(loginAccount)
        assertNotNull(loginAccount)
    }

    @Test
    fun isAccountActive(){
        val loginAccount = twitterLoginAccountRepo.findById(twitterIdentifier).orElse(null)
        //val loginAccount = twitterLoginAccountRepo.findOne(twitterIdentifier)
        val cb = ConfigurationBuilder()
                .setDebugEnabled(true)
                .setOAuthConsumerKey(loginAccount.consumerKey)
                .setOAuthConsumerSecret(loginAccount.consumerSecret)
                .setOAuthAccessToken(loginAccount.accessToken)
                .setOAuthAccessTokenSecret(loginAccount.accessTokenSecret)
        val tf = TwitterFactory(cb.build())
        val twitter = tf.instance
        val responseList = twitter.getUserTimeline(18762875)
        assertFalse(responseList.isEmpty())
    }


}