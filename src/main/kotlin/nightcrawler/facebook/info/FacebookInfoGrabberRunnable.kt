package nightcrawler.facebook.info

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IQueue
import nightcrawler.crawler.Crawler
import nightcrawler.database.mongodb.models.FacebookLoginAccount
import nightcrawler.database.mongodb.models.FacebookTargetMongo
import nightcrawler.database.mongodb.models.FacebookTargetMongoMissionControl
import nightcrawler.database.neo4j.models.*
import nightcrawler.database.neo4j.repositories.FacebookBusinessPageRepository
import nightcrawler.database.neo4j.splitLoadersAndSavers.FacebookAccountSplitLoader
import nightcrawler.facebook.differencemonitor.DifferenceMailBuilder
import nightcrawler.facebook.info.bancheck.FacebookBanCheckerFacebook
import nightcrawler.facebook.info.infograbber.exceptions.FacebookAccountDoesNotExistsException
import nightcrawler.facebook.info.infograbber.infograbbers.*
import nightcrawler.facebook.info.infograbber.timeline.TimelineGrabber
import nightcrawler.facebook.login.FacebookLoginner
import nightcrawler.facebook.targetmanager.MongoTargetManager
import nightcrawler.mail.DailyMailReportBuilder
import nightcrawler.springconfiguration.ApplicationContextProvider
import nightcrawler.utils.conf
import nightcrawler.utils.log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Callable
import javax.annotation.PostConstruct
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet

/**
 * Created by andrea on 26/07/16.
 */
@Component
@Scope("prototype")
class FacebookInfoGrabberRunnable(val ft: FacebookTargetMongo) : Runnable, Callable<FacebookPage?> {


    @Autowired
    private lateinit var ctx: ApplicationContext

    @Autowired
    private lateinit var hz: HazelcastInstance

    @Autowired
    private lateinit var facebookTargetManagerMongo: MongoTargetManager

    @Autowired
    lateinit var facebookNeo4jEntityFactory: FacebookNeo4jEntityFactory

    @Autowired
    lateinit var facebookAccountSplitLoader: FacebookAccountSplitLoader

    @Autowired
    private lateinit var facebookBusinessPageRepo: FacebookBusinessPageRepository

    @Autowired
    private lateinit var facebookTargetMissionController: FacebookTargetMissionController

    private lateinit var targetsInProcessQueue: IQueue<FacebookTargetMongo>


    @PostConstruct
    fun init() {
        targetsInProcessQueue = hz.getQueue(conf.getString("socialnet.hazelcastQueues.targetsInProcess"))
    }

    //used for testing purposes
    override fun call(): FacebookPage? {
        return this.processTarget(this.ft)
    }

    override fun run() {
        if (targetsInProcessQueue.contains(ft)) {
            log().warn("FacebookTarget ${ft.facebookIdentifier} is already in process. SKIPPIN IT")
            return
        }
        targetsInProcessQueue.put(ft)
        try {
            if ((MongoTargetManager.updateTimeLineNecessary(ft) || MongoTargetManager.updateAttributesNecessary(ft)) || ft.basicInfoOnly || ft.priority > 0) {
                log().info("STARTED infograbber for ft: ${ft.facebookIdentifier}")
                processTarget(ft)
                log().info("FINISHED infograbber for ft: ${ft.facebookIdentifier}")
            } else {
                log().info("No update is necessary for ft ${ft.facebookIdentifier}")
            }
        } finally {
            val targetToBeRemoved = targetsInProcessQueue.find { it.facebookIdentifier == ft.facebookIdentifier }
            val removeBool = targetsInProcessQueue.remove(targetToBeRemoved)
            if (!removeBool) log().error("Target ${ft.facebookIdentifier} has not been removed from targetsInProcess queue")
        }
    }

    fun processTarget(ft: FacebookTargetMongo): FacebookPage? {
        var fp: FacebookPage? = null
        try {
            fp = ctx.getBean(BasicInfoGrabber::class.java, ft).process()
            if (!fp.doesExist) {
                log().warn("Page ${fp.facebookName} does not exist. No further elaboration is required.")
                return fp
            }
            fp.isTarget = ft.saveInDb
            fp = facebookNeo4jEntityFactory.saveFacebookAccount(fp, 0)
            facebookTargetManagerMongo.updateTarget(ft)
            if (ft.basicInfoOnly) return fp
            val newPosts: Set<FacebookPost>?
            when (fp) {
                is FacebookAccount -> {
                    fp = facebookAccountSplitLoader.load(fp)
                    extractUserAttributes(ft, fp)
                    newPosts = extractTimeline(ft, fp)
                }
                is FacebookGroup -> {
                    extractGroupAttributes(ft, fp)
                    newPosts = extractTimeline(ft, fp)
                }
                is FacebookBusinessPage -> {
                    extractBusinessPageAttributes(ft, fp)
                    newPosts = extractTimeline(ft, fp)
                }
                else -> {
                    ft.lastUpdateAttributes = LocalDateTime.now() // there is no need to update attributes on a page (there aren't) so update the date
                    newPosts = extractTimeline(ft, fp)
                }
            }
            fp.hydrationDate = Date()
            DifferenceMailBuilder.buildAndSendDifferenceMail(ft, facebookNeo4jEntityFactory.getOrCreateFacebookPageFromTarget(ft, fp::class.java), fp, newPosts?: HashSet())
            facebookNeo4jEntityFactory.saveFacebookAccount(fp, 1)
            DailyMailReportBuilder.accountProcessedToday.add(fp.facebookName ?: ft.facebookIdentifier!!)
            if (ft.saveInDb) facebookTargetManagerMongo.decreasePriorityAndSave(ft)
            facebookTargetMissionController.missionControl(ft, fp, newPosts ?: HashSet())

        } catch (e: Exception) {
            log().error("Exception during FacebookInfoGrabber Thread of : ${ft.facebookIdentifier}", e)
        }
        return fp
    }


    private fun extractGroupAttributes(ft: FacebookTargetMongo, fg: FacebookGroup) {
        if (MongoTargetManager.updateAttributesNecessary(ft)) {
            updateAttributesFacebookGroup(fg)
            ft.lastUpdateAttributes = LocalDateTime.now()
            if (ft.saveInDb) facebookTargetManagerMongo.updateTarget(ft)
        }
    }

    private fun extractUserAttributes(ft: FacebookTargetMongo, fa: FacebookAccount) {
        if (ft.priority > 0 || MongoTargetManager.updateAttributesNecessary(ft)) {
            updateAttributesFacebookAccount(fa)
            ft.lastUpdateAttributes = LocalDateTime.now()
            if (ft.saveInDb) facebookTargetManagerMongo.updateTarget(ft)
        } else {
            log().debug("Update attributes (FRIENDS,LIKES,etc.) is not necessary for facebookAccount: ${fa.facebookName}")
        }
    }

    private fun extractTimeline(ft: FacebookTargetMongo, fa: FacebookPage): Set<FacebookPost>? {
        var newPosts: Set<FacebookPost>? = null
        if (MongoTargetManager.updateTimeLineNecessary(ft) || ft.priority > 0) {
            newPosts = ctx.getBean(TimelineGrabber::class.java, fa.url
                    ?: baseUrlFromFacebookAccount(fa)).process(eagerTimeline = ft.lastUpdateTimeline == null || ft.eagerTimeline) //if firstTime scroll to bottom
            ft.lastUpdateTimeline = LocalDateTime.now()
            facebookTargetManagerMongo.updateTarget(ft)
            DailyMailReportBuilder.timelinesProcessedToday.add(fa.facebookName ?: ft.facebookIdentifier!!)
        } else {
            log().debug("Update timeline is not necessary for facebookAccount: ${fa.facebookName}")
        }
        return newPosts
    }


    fun updateAttributesFacebookAccount(fa: FacebookAccount) {
        ctx.getBean(ExperienceGrabber::class.java, fa).process()
        ctx.getBean(HasLivedInGrabber::class.java, fa).process()
        ctx.getBean(LikesGrabber::class.java, fa).process()
        ctx.getBean(FriendsGrabber::class.java, fa).process()
        ctx.getBean(FollowersGrabber::class.java, fa).process()
        ctx.getBean(FollowingGrabber::class.java, fa).process()
        fa.hydrationDate = Date()
    }


    fun updateAttributesFacebookGroup(fg: FacebookGroup) {
        ctx.getBean(GroupMembersGrabber::class.java, fg).process()
        fg.hydrationDate = Date()
    }

    fun extractBusinessPageAttributes(ft: FacebookTargetMongo, fp: FacebookBusinessPage) {
        ctx.getBean(PageLikersGrabber::class.java, fp).process()
        fp.hydrationDate = Date()
    }



}