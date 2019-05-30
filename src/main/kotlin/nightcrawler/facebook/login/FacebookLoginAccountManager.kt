package nightcrawler.facebook.login

import nightcrawler.crawler.webdriver.Cookie
import nightcrawler.database.mongodb.models.FacebookLoginAccount
import nightcrawler.database.mongodb.repositories.FacebookLoginAccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

/**
 * Created by andrea on 27/07/16.
 */

@Component
@Scope("singleton")
class FacebookLoginAccountManager {

    @Autowired
    lateinit var facebookLoginAccountRepository: FacebookLoginAccountRepository


    fun getAllAvailableAccounts(): List<FacebookLoginAccount> {
        synchronized(this){
            val allAvailableAccounts: List<FacebookLoginAccount> = facebookLoginAccountRepository.findAll()
            restoreUnbannedAccount(allAvailableAccounts)
            return allAvailableAccounts
        }
    }

    fun getAllFunctioningAccount(): List<FacebookLoginAccount> {
        var allAccounts = facebookLoginAccountRepository.findAll()
        allAccounts = allAccounts.filter { it.ips.isNotEmpty() }
        return allAccounts
    }

    fun getAllTelephoneNumberFinderEnabled(): List<FacebookLoginAccount> {
        var allAccounts = facebookLoginAccountRepository.findAll()
        allAccounts = allAccounts.filter { it.telephoneNumberFinderEnabled ?: false }
        return allAccounts
    }

    fun getRandomTelephoneNumberFinderEnabledAccount(): FacebookLoginAccount{
        val list = getAllTelephoneNumberFinderEnabled()
        return list[(Math.random() * list.size).toInt()]
    }

    fun getAllAvailableAccountByIp(ip: String): List<FacebookLoginAccount> {
        val allAvailableAccounts = getAllAvailableAccounts()
        return allAvailableAccounts.filter({ fla -> fla.ips.contains(ip) && !loginAccountIsTemporaryBanned(fla) })
    }


    fun restoreUnbannedAccount(loginAccountList: List<FacebookLoginAccount>) {
        loginAccountList.forEach { fla ->
            if (fla.debanDate != null && loginAccountIsTemporaryBanned(fla)) {
                fla.debanDate = null
                facebookLoginAccountRepository.save(fla)
            }
        }
    }

    fun loginAccountIsTemporaryBanned(fla: FacebookLoginAccount): Boolean {
        return fla.debanDate?.isAfter(LocalDateTime.now()) ?: false
    }


}