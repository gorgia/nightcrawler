package nightcrawler.procedures

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IQueue
import nightcrawler.TestConfiguration
import nightcrawler.crawler.Crawler
import nightcrawler.database.neo4j.models.*
import nightcrawler.database.neo4j.repositories.FacebookEntityRepository
import nightcrawler.database.neo4j.repositories.FacebookPageRepository
import nightcrawler.facebook.info.FacebookInfoGrabberRunnable
import nightcrawler.facebook.info.FacebookNeo4jEntityFactory
import nightcrawler.facebook.info.baseUrlFromFacebookName
import nightcrawler.facebook.info.infograbber.exceptions.FacebookAccountDoesNotExistsException
import nightcrawler.facebook.info.infograbber.infograbbers.BasicInfoGrabber
import nightcrawler.facebook.info.infograbber.timeline.TimelineGrabber
import nightcrawler.facebook.login.FacebookLoginner
import nightcrawler.facebook.targetmanager.MongoTargetManager
import nightcrawler.springconfiguration.ApplicationContextProvider
import nightcrawler.utils.conf
import nightcrawler.utils.log
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.time.LocalDateTime
import java.util.*
import javax.annotation.PostConstruct

/**
 * Created by andrea on 31/08/16.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = arrayOf("scheduling.enabled=false"), classes = arrayOf(TestConfiguration::class))
class CorrectNeo4jErrors {

    @Autowired
    lateinit var facebookNeo4jEntityFactory: FacebookNeo4jEntityFactory

    @Autowired
    lateinit var facebookEntityRepository: FacebookEntityRepository

    @Autowired
    lateinit var facebookPageRepository: FacebookPageRepository

    @Autowired
    lateinit var crawler: Crawler

    @Autowired
    lateinit var facebookLoginner: FacebookLoginner

    @Autowired
    lateinit var ctx: ApplicationContext


    @Autowired
    lateinit var targetManager: MongoTargetManager


    val alreadyVerified: HashSet<String?> = HashSet()


    @Test
    fun correct(){
        selectEntities<FacebookEntity>().toHashSet().forEach{
            log().info("prova")
            val newfacebookId = Regex("[^0-9]+").find(it.facebookId!!)!!.value
            it.facebookId = newfacebookId
            val entityInDB : FacebookEntity? = facebookNeo4jEntityFactory.getByFacebookId(facebookId = it.facebookId!!, clazz = FacebookEntity::class.java, completeLoad = false)
            if(entityInDB != null && entityInDB is FacebookPage) {
                val ft = targetManager.getOrCreateTargetFromFacebookIdentifer(entityInDB.facebookName!!)
                val fp = ctx.getBean(BasicInfoGrabber::class.java, ft).process()
                facebookPageRepository.save(fp)
            }
            facebookEntityRepository.save(it)
        }
    }

    fun <T> selectEntities() : Iterable<T>{
        return facebookEntityRepository.findWithQuestionMark() as Iterable<T>
    }


/*
    open fun correctFacebookPost(post: FacebookPost) {
        val timelineGrabber = ApplicationContextProvider.ctx!!.getBean(TimelineGrabber::class.java, crawler, "https://facebook.com/${post.facebookId}")
        val correctedPost = timelineGrabber.process(true, true)
        if (correctedPost == null || correctedPost.isEmpty()) {
            facebookEntityRepository.deleteNode(post.id!!)
        }
    }


    @Test
    @Ignore
    open fun correctLabels() {
        facebookLoginner.login(crawler)
        do {
            val withoutLabels = facebookEntityRepository.findWithoutLabels()
            var processingNode: FacebookEntity? = null
            try {
                withoutLabels.forEach {
                    processingNode = it
                    val ft = createFacebookTargetNeo4jFromEntity(it)
                    val basicInfoGrabber = ApplicationContextProvider.ctx!!.getBean(BasicInfoGrabber::class.java, crawler, ft, baseUrlFromFacebookName(ft.facebookIdentifier!!))
                    try {
                        val facebookPage = basicInfoGrabber.process()
                        facebookNeo4jEntityFactory.saveFacebookAccount(facebookPage, 0)
                    } catch(e: FacebookAccountDoesNotExistsException) {
                        log().error("Page at url: ${baseUrlFromFacebookName(ft.facebookIdentifier!!)} does not exists")
                        facebookEntityRepository.deleteNode(it.id!!)
                    }
                }
            } catch (e: Exception) {
                log().error("Error during processing of: ${processingNode?.facebookId}", e)
                Thread.sleep(30000)
            }
        } while (withoutLabels.toList().isNotEmpty())
    }


    open fun createFacebookTargetNeo4jFromEntity(fe: FacebookEntity): FacebookTargetNeo4j {
        val fp = FacebookPage()
        fp.facebookId = fe.facebookId
        fp.doesExists = fe.doesExists
        fp.url = fe.url
        fp.id = fe.id
        return facebookTargetManger.createFacebookTargetNeo4jFromFacebookPage(fp)
    }

    @Test
    open fun correctFacebookIdMissing() {
        val targetPages: List<FacebookPage> = facebookPageRepository.findByFacebookIdNull()
        targetPages.forEach {
            val t = createFacebookTargetNeo4jFromEntity(it)
            t.saveInDb = false
            t.lastUpdateTimeline = LocalDateTime.now().plusWeeks(2)
            t.lastUpdateAttributes = LocalDateTime.now().plusWeeks(2)
            t.basicInfoOnly = true
            targetsToBeProcessedQueue.add(t)
        }
        Thread.sleep(10000000)
    }
*/
}