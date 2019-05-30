package nightcrawler.facebook.targetmanager

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IQueue
import com.mongodb.DuplicateKeyException
import nightcrawler.database.mongodb.models.FacebookTargetMongo
import nightcrawler.database.mongodb.models.FacebookTargetMongoMissionControl
import nightcrawler.database.mongodb.repositories.FacebookTargetRepository
import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.database.neo4j.repositories.FacebookPageRepository
import nightcrawler.facebook.info.FacebookNeo4jEntityFactory
import nightcrawler.facebook.info.extractFacebookNameFromHref
import nightcrawler.utils.conf
import nightcrawler.utils.log
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*
import javax.annotation.PostConstruct

/**
 * Created by andrea on 22/12/16.
 */

@Component
class MongoTargetManager {

    @Autowired
    lateinit var facebookTargetRepository: FacebookTargetRepository

    @Autowired
    lateinit var facebookNeo4jEntityFactory: FacebookNeo4jEntityFactory

    @Autowired
    lateinit var facebookPageRepo: FacebookPageRepository

    @Qualifier("hazelcastInstance")
    @Autowired
    private lateinit var hz: HazelcastInstance

    @Autowired
    private lateinit var jarvisTargetProvider: JarvisTargetProvider

    lateinit var targetsToBeProcessedQueue: IQueue<FacebookTargetMongo>
    lateinit var targetsInProcessQueue: IQueue<FacebookTargetMongo>


    @PostConstruct
    fun init() {
        targetsToBeProcessedQueue = hz.getQueue<FacebookTargetMongo>(conf.getString("socialnet.hazelcastQueues.targetswaitingtobeprocessed"))
        targetsInProcessQueue = hz.getQueue<FacebookTargetMongo>(conf.getString("socialnet.hazelcastQueues.targetsInProcess"))
    }


    @Synchronized
    fun getNextTarget(targetList: List<FacebookTargetMongo> = ArrayList()): FacebookTargetMongo {
        var nextTarget: FacebookTargetMongo? = null //getPriorityTarget()
        while (nextTarget == null) {
            targetsToBeProcessedQueue.addAll(fillPriorityTargetCacheFromMongo())
            if (targetsToBeProcessedQueue.isEmpty())
                if (targetList.isNotEmpty()) targetsToBeProcessedQueue.addAll(targetList)
            if (targetsToBeProcessedQueue.isEmpty())
                targetsToBeProcessedQueue.addAll(fillTargetCacheFromMongo())
            if (targetsToBeProcessedQueue.isEmpty()) {
                log().info("Looks like all targets in mongo have already been processed.I have some spare time... I will process an interesting unprocessed account")
                fillTargetWithInterestingAccounts()
            }

            nextTarget = targetsToBeProcessedQueue.poll()
            if (nextTarget == null) {
                log().warn("Looks Like I have nothing to do! I will sleep for 30 minutes")
                Thread.sleep(30 * 1000 * 60)
            }
        }
        return nextTarget
    }

    fun updateTarget(target: FacebookTargetMongo): FacebookTargetMongo {
        try {
            if (target.saveInDb) return facebookTargetRepository.save(target)
        } catch (e: DuplicateKeyException) {
            log().error("Duplicate key in mongodb for _id: ${target.facebookIdentifier} and neo4jnodeID: ${target.neo4jNodeId}")
        }
        return target
    }

    private fun fillPriorityTargetCacheFromMongo(): List<FacebookTargetMongo> {
        var priorityTargetList = facebookTargetRepository.findByPriorityGreaterThan()
        val idsTargetInProcess: HashSet<String> = HashSet()
        val idsTargetToBeProcessed: HashSet<String> = HashSet()
        targetsInProcessQueue.mapNotNullTo(idsTargetInProcess, FacebookTargetMongo::facebookIdentifier)
        targetsToBeProcessedQueue.mapNotNullTo(idsTargetToBeProcessed) { it.facebookIdentifier }
        priorityTargetList = priorityTargetList.filterNot { idsTargetInProcess.contains(it.facebookIdentifier) || idsTargetToBeProcessed.contains(it.facebookIdentifier) || targetsInProcessQueue.contains(it) }
        priorityTargetList = priorityTargetList.sortedBy { -it.priority }
        return priorityTargetList
    }

    private fun fillTargetCacheFromMongo(): List<FacebookTargetMongo> {
        var targetList = facebookTargetRepository.findAll()
        val idsTargetInProcess: HashSet<Long> = HashSet()
        val idsTargetToBeProcessed: HashSet<Long> = HashSet()
        targetsInProcessQueue.mapNotNullTo(idsTargetInProcess, FacebookTargetMongo::neo4jNodeId)
        targetsToBeProcessedQueue.mapNotNullTo(idsTargetToBeProcessed) { it.neo4jNodeId }
        targetList = targetList.filterNot { idsTargetInProcess.contains(it.neo4jNodeId) || idsTargetToBeProcessed.contains(it.neo4jNodeId) || targetsInProcessQueue.contains(it) }
        targetList = targetList.filter { updateAttributesNecessary(it) || updateTimeLineNecessary(it) || it.priority > 0 }
        targetList = targetList.sortedBy { -it.priority }
        //removing duplicates
        return targetList
    }

    private fun fillTargetWithInterestingAccounts() {
        val targetsToBeExcluded = facebookTargetRepository.findAll().toMutableList()
        targetsToBeExcluded.addAll(targetsInProcessQueue)
        targetsToBeExcluded.addAll(targetsToBeProcessedQueue)
        val newTargetList = jarvisTargetProvider.getMostConnectedNotHydratedTargetsMongo(targetsToBeExcluded)
        newTargetList.forEach {
            it.lastUpdateTimeline = LocalDateTime.now().plusWeeks(10)
            it.saveInDb = false
        } //I don't want to fill the db with uninteresting posts or automated choices.
        val set = HashSet<FacebookTargetMongo>()
        set.addAll(newTargetList)
        targetsToBeProcessedQueue.addAll(set)
    }


    fun getOrCreateTargetFromFacebookIdentifer(facebookIdentifier: String, saveInDb: Boolean = false, priority: Int = 0, missionControl: FacebookTargetMongoMissionControl? = null): FacebookTargetMongo {
        var cleanedFacebookIdentifier: String = facebookIdentifier
        if (cleanedFacebookIdentifier.startsWith("https://www.facebook.com")) cleanedFacebookIdentifier = extractFacebookNameFromHref(facebookIdentifier)!!
        var t = facebookTargetRepository.findById(facebookIdentifier).orElse(null)
        //var t = facebookTargetRepository.findOne(cleanedFacebookIdentifier)
        if (t != null) return t
        val facebookPage: FacebookPage? = if (StringUtils.isNumeric(cleanedFacebookIdentifier))
            facebookPageRepo.findByFacebookIdOrFacebookName(cleanedFacebookIdentifier, "profile.php?id=$cleanedFacebookIdentifier")
        else
            facebookPageRepo.findByFacebookIdOrFacebookName(cleanedFacebookIdentifier, cleanedFacebookIdentifier)
        if (facebookPage != null) t = facebookTargetRepository.findByNeo4jNodeId(facebookPage.id!!)
        if (t != null) return t
        t = FacebookTargetMongo()
        t.facebookIdentifier = cleanedFacebookIdentifier.toLowerCase()
        t.neo4jNodeId = facebookPage?.id
        t.updateTimelineEveryMinutes = 28800
        t.updateAttributesEveryDays = 120
        if (missionControl != null) {
            t.facebookTargetMongoMissionControl = missionControl.copy(depth = missionControl.depth - 1)
            t.priority = arrayOf(priority, missionControl.priority).max() ?: 0
        } else t.priority = priority

        if (saveInDb) {
            t = facebookTargetRepository.save(t)
        }
        return t
    }

    fun decreasePriorityAndSave(ft: FacebookTargetMongo, quantity: Int = 1) {
        if(!ft.saveInDb) return
        ft.priority = ft.priority - quantity
        if (ft.priority < 0) ft.priority = 0
        facebookTargetRepository.save(ft)
    }

    companion object {
        fun updateAttributesNecessary(target: FacebookTargetMongo): Boolean {
            return target.lastUpdateAttributes?.isBefore(LocalDateTime.now().minusDays(target.updateAttributesEveryDays)) ?: true
        }

        fun updateTimeLineNecessary(target: FacebookTargetMongo): Boolean {
            val now = LocalDateTime.now()
            val nowminusminutes = now.minusMinutes(target.updateTimelineEveryMinutes)
            val res = target.lastUpdateTimeline?.isBefore(nowminusminutes) ?: true
            return res
        }
    }
}