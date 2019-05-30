package nightcrawler.twitter.databases.mongo

import nightcrawler.twitter.databases.mongo.models.TwitterTarget
import nightcrawler.twitter.databases.mongo.repo.TwitterTargetRepository
import nightcrawler.twitter.databases.neo4j.repositories.TwitterAccountRepository
import nightcrawler.utils.log
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * Created by andrea on 01/02/17.
 */
@Component
open class TwitterTargetManager {

    @Autowired
    lateinit var twitterTargetRepo: TwitterTargetRepository

    @Autowired
    lateinit var twitterAccountRepo: TwitterAccountRepository


    open fun getNextTwitterTarget(): TwitterTarget {
        var filteredTts: MutableList<TwitterTarget>
        do {
            val tts = twitterTargetRepo.findAll()
            filteredTts = tts.filter { updateAttributesNecessary(it) || updateTimeLineNecessary(it) }.toMutableList()
            if (filteredTts.isEmpty()) {
                log().info("There are no twitterTargets. Sleep for an hour")
                Thread.sleep(3600 * 1000)
            }
            filteredTts.sortBy { -it.priority }
        } while (filteredTts.isEmpty())
        return filteredTts.first()
    }

    open fun getOrCreateTargetFromTwitterIdentifier(twitterIdentifier: String, isQuery: Boolean = false, saveInDb: Boolean = false): TwitterTarget {
        //var t = twitterTargetRepo.findById(twitterIdentifier).orElse(null)
        var t = twitterTargetRepo.findById(twitterIdentifier).orElse(null)
        if (t != null) return t
        t = TwitterTarget()
        t.twitterIdentifier = twitterIdentifier
        t.isQuery = isQuery
        t.neo4jNodeId = twitterAccountRepo.findByName(twitterIdentifier)?.id ?: if (StringUtils.isNumeric(twitterIdentifier)) twitterAccountRepo.findByTwitterId(twitterIdentifier.toLong())?.id else null
        if (saveInDb) {
            t = twitterTargetRepo.save(t)
        }
        return t
    }

    open fun decreasePriorityAndSave(tt: TwitterTarget) {
        tt.priority--
        twitterTargetRepo.save(tt)
    }

    fun forgetForWeeks(tt: TwitterTarget, numberOfWeeks: Int = 2) {
        if (!tt.isQuery) tt.lastUpdateAttributes = LocalDateTime.now().plusDays(14)
        tt.lastUpdateTimeline = LocalDateTime.now().plusDays(14)
        twitterTargetRepo.save(tt)
    }

    companion object {
        fun updateAttributesNecessary(target: TwitterTarget): Boolean {
            if (target.isQuery) return false
            return target.lastUpdateAttributes?.isBefore(LocalDateTime.now().minusDays(target.updateAttributesEveryDays)) ?: true
        }

        fun updateTimeLineNecessary(target: TwitterTarget): Boolean {
            return target.lastUpdateTimeline?.isBefore(LocalDateTime.now().minusMinutes(target.updateTimelineEveryMinutes)) ?: true
        }
    }
}