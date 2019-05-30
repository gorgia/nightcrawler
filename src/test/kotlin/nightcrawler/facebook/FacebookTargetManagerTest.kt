package nightcrawler.facebook

import com.google.common.io.Resources
import nightcrawler.TestConfiguration
import nightcrawler.database.mongodb.models.FacebookTargetMongoMissionControl
import nightcrawler.database.neo4j.repositories.FacebookPageRepository
import nightcrawler.facebook.info.extractFacebookNameFromHref
import nightcrawler.facebook.targetmanager.JarvisTargetProvider
import nightcrawler.facebook.targetmanager.MongoTargetManager
import nightcrawler.utils.log
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File
import java.time.LocalDateTime
import kotlin.test.assertFalse

/**
 * Created by andrea on 17/08/16.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = arrayOf("scheduling.enabled=false"), classes = arrayOf(TestConfiguration::class))
class FacebookTargetManagerTest {

    @Autowired
    lateinit var facebookTargetManager: MongoTargetManager

    @Autowired
    lateinit var facebookPageRepository: FacebookPageRepository

    @Autowired
    lateinit var jarvishTargetProvider: JarvisTargetProvider

    val fileName = "holiday.txt"

    @Test
    @Ignore
    fun correct() {
        val restartDate = LocalDateTime.now().minusDays(80)
        val targetList = facebookTargetManager.facebookTargetRepository.findAll()
        targetList.forEach { t ->
            t.lastUpdateTimeline = restartDate
            t.lastUpdateAttributes = restartDate
            facebookTargetManager.facebookTargetRepository.save(t)
        }
        assertFalse { targetList.isEmpty() }
    }

    @Test
    fun addFromList() {
        val url = Resources.getResource(fileName)
        val uri = url.toURI()
        val file = File(uri)
        val lines = file.readLines()
        lines.forEach { identifier ->
                val ft = facebookTargetManager.getOrCreateTargetFromFacebookIdentifer(identifier.toLowerCase())
                ft.saveInDb = true
                ft.lastUpdateAttributes = null
                ft.lastUpdateTimeline = null
                ft.priority = 3
                ft.creationDate = LocalDateTime.now()
                ft.facebookTargetMongoMissionControl = FacebookTargetMongoMissionControl("holiday", 1)
                facebookTargetManager.updateTarget(ft)
                if (ft.neo4jNodeId != null) {
                    val fp = facebookPageRepository.findById(ft.neo4jNodeId, 0).get()
                    // val fp = facebookPageRepository.findOne(ft.neo4jNodeId, 0)
                    fp.isTarget = true
                    facebookPageRepository.save(fp)
                }
                log().info("Added to DB  targetIdentifier : ${ft.facebookIdentifier}")
            }
        }



    @Test
    @Ignore
    fun resetUpdateTimelineAndAttributes() {
        val url = Resources.getResource(fileName)
        val uri = url.toURI()
        val file = File(uri)
        val lines = file.readLines()
        lines.forEach { line ->
            val target = facebookTargetManager.facebookTargetRepository.findById(line).get()
            //val target = facebookTargetManager.facebookTargetRepository.findOne(line)
            target.lastUpdateAttributes = null
            target.lastUpdateTimeline = null
            facebookTargetManager.facebookTargetRepository.save(target)
        }
    }

    @Test
    @Ignore
    fun signAsTarget() {
        val targetList = facebookTargetManager.facebookTargetRepository.findAll()
        targetList.forEach {
            if (it.neo4jNodeId != null) {
                val facebookPage = facebookPageRepository.findById(it.neo4jNodeId).get()
                //val facebookPage = facebookPageRepository.findOne(it.neo4jNodeId)
                facebookPage.isTarget = true
                facebookPageRepository.save(facebookPage, 0)
            }
        }
    }


}