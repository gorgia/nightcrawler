package nightcrawler.twitter.webcrawler.login

import nightcrawler.twitter.databases.mongo.models.TwitterLoginAccount
import nightcrawler.twitter.databases.mongo.repo.TwitterLoginAccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

/**
 * Created by andrea on 27/02/17.
 */
@Component
open class TwitterLoginAccountManager {

    @Autowired
    lateinit var twitterLoginAccountRepo: TwitterLoginAccountRepository


    fun getRandomLoginAccount(): TwitterLoginAccount {
        val twitterLoginAccountList = getNotRateLimitedAccount()
        Collections.shuffle(twitterLoginAccountList)
        return twitterLoginAccountList.first()
    }

    fun saveTwitterLoginAccount(twitterLoginAccount: TwitterLoginAccount) {
        twitterLoginAccountRepo.save(twitterLoginAccount)
    }

    fun setAccountRateLimitedUntil(untilDate: LocalDateTime, twitterLoginAccount: TwitterLoginAccount) {
        twitterLoginAccount.isRateLimitedUntil = untilDate
        twitterLoginAccountRepo.save(twitterLoginAccount)
    }

    fun getNotRateLimitedAccount(): List<TwitterLoginAccount> {
        val notRateLimitedAccount: MutableList<TwitterLoginAccount> = ArrayList()
        val twitterLoginAccountList = twitterLoginAccountRepo.findAll()
        twitterLoginAccountList.forEach {
            if (it.isRateLimitedUntil != null && it.isRateLimitedUntil!!.isBefore(LocalDateTime.now())) {
                it.isRateLimitedUntil = null
                twitterLoginAccountRepo.save(it)
            }
            if (it.isRateLimitedUntil == null) notRateLimitedAccount.add(it)
        }
        return notRateLimitedAccount
    }

}