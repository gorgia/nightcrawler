package nightcrawler.facebook.targetmanager

import com.google.common.io.Resources
import nightcrawler.TestConfiguration
import nightcrawler.database.mongodb.repositories.FacebookTargetRepository
import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.facebook.info.FacebookNeo4jEntityFactory
import nightcrawler.facebook.info.extractFacebookNameFromHref
import nightcrawler.utils.log
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File
import java.util.*

/**
 * Created by andrea on 02/01/17.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = arrayOf("scheduling.enabled=false"), classes = arrayOf(TestConfiguration::class))
class MongoTargetManagerTest {

    @Autowired
    lateinit var facebookTargetRepository: FacebookTargetRepository

    @Autowired
    lateinit var facebookNeo4jEntityFactory: FacebookNeo4jEntityFactory

    @Autowired
    lateinit var targetManager: MongoTargetManager

    val filename = "telefono11-2017facebookid"
    val updatemail = "petrastrana@gmail.com"

    @Test
    @Ignore
    fun checkTargets() {
        val targetIdList = obtainList()
        var count = 0
        targetIdList.forEach {
            val target = targetManager.getOrCreateTargetFromFacebookIdentifer(it)
            if (!target.updateMailList!!.contains("petrastrana@gmail.com")) {
                log().warn("Target: ${target.facebookIdentifier} did not contains update mail but contains ${target.updateMailList}")
                target.updateMailList!!.add(updatemail)
                facebookTargetRepository.save(target)
            }
            log().info("Target processed: ${++count} - $it")
        }
    }

    @Test
    @Ignore
    fun petrastranaTargets() {
        val targets = facebookTargetRepository.findAll()
        val filteredTargets = targets.filter { it.updateMailList?.contains("petrastrana@gmail.com") ?: false }
        val neo4jNodeIdSet: MutableSet<Long?> = HashSet()
        File("citatidallaproduzione.txt").printWriter().use { out ->
            filteredTargets.forEach { target ->
                val neo4jEntity = facebookNeo4jEntityFactory.getOrCreateFacebookPageFromTarget(target, FacebookPage::class.java)
                if (neo4jNodeIdSet.contains(target.neo4jNodeId)) log().error("Target :${target.facebookIdentifier} have the same neo4j node id of another. Neo4jNodeId duplicate: ${target.neo4jNodeId}")
                else neo4jNodeIdSet.add(target.neo4jNodeId)
                if (target.neo4jNodeId != neo4jEntity.id) log().error("target: ${target.facebookIdentifier} points to different node. neo4j node for facebookID is: id: ${neo4jEntity.id} facebookID: ${neo4jEntity.facebookId}")
                out.println("targetID: ${target.facebookIdentifier}, facebookId: ${neo4jEntity.facebookId}, facebookName: ${neo4jEntity.facebookName}")
            }
        }
    }

    @Ignore
    @Test
    fun batchImportTarget() {
        val stringList = obtainList()
        val idSet: MutableSet<String> = HashSet()
        stringList.forEach {
            val xml11pattern: Regex = Regex("[^\\p{ASCII}]")
            var facebookIdentifier = it.replace(xml11pattern, "")
            facebookIdentifier = facebookIdentifier.replace("%","")
            try {
                facebookIdentifier = extractFacebookNameFromHref(facebookIdentifier)!!
            } catch(e: Exception) {
                log().error("unable to extract facebookName from: ${facebookIdentifier}", e)
            }
            if (facebookIdentifier.contains('C'))
                if (facebookIdentifier.length - facebookIdentifier.indexOf('C') < 6) facebookIdentifier = facebookIdentifier.substring(0, facebookIdentifier.indexOf('C'))

            idSet.add(facebookIdentifier)
        }
        File("step2.txt").bufferedWriter().use { out ->
            idSet.forEach {
                out.write(it +"\n")
            }
        }
    }


    fun obtainList(): List<String> {
        val url = Resources.getResource(filename)
        val uri = url.toURI()
        val file = File(uri)
        val lines = file.readLines()
        return lines
    }
}