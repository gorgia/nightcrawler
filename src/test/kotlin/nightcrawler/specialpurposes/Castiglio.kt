package nightcrawler.specialpurposes

import nightcrawler.TestConfiguration
import nightcrawler.database.neo4j.models.FacebookBusinessPage
import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.database.neo4j.repositories.FacebookBusinessPageRepository
import nightcrawler.facebook.info.FacebookInfoGrabberRunnable
import nightcrawler.facebook.info.FacebookNeo4jEntityFactory
import nightcrawler.facebook.info.infograbber.infograbbers.BasicInfoGrabber
import nightcrawler.facebook.info.infograbber.infograbbers.SearchGrabber
import nightcrawler.facebook.targetmanager.MongoTargetManager
import nightcrawler.utils.log
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = arrayOf("scheduling.enabled=false"), classes = [(TestConfiguration::class)])
class Castiglio {

    @Autowired
    lateinit private var ctx: ApplicationContext

    @Autowired
    lateinit var infoGrabberExecutor: ThreadPoolTaskExecutor

    @Autowired
    lateinit var neo4jEntityFactory: FacebookNeo4jEntityFactory

    @Autowired
    lateinit var targetManger: MongoTargetManager

    @Autowired
    lateinit var facebookBusinessPageRepo: FacebookBusinessPageRepository


    val targetsArray = arrayOf("insert facebook id here")

    @Test
    fun prova() {
        val pairList = creaCoppie()
        val pool = Executors.newFixedThreadPool(2)
        pairList.forEach { pool.submit({ this.eseguiRicerca(it) }) }
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)
        } catch (e: Exception) {
            log().error("", e)
        }
    }


    fun creaCoppie(): MutableList<Pair<String, String>> {
        val pairList: MutableList<Pair<String, String>> = ArrayList()
        for (i in 0 until targetsArray.size) {
            for (j in i + 1 until targetsArray.size) {
                // code to print a[i] - a[j]
                pairList.add(Pair(targetsArray[i], targetsArray[j]))
            }
        }
        return pairList
    }

    fun eseguiRicerca(coppiaDiId: Pair<String, String>) {
        val url = "https://www.facebook.com/search/${coppiaDiId.first}/likers/${coppiaDiId.second}/likers/intersect"
        File("output-destra.txt").appendText(url + "\n")

        val likers = ctx.getBean(SearchGrabber::class.java).process(url)
        if (!likers.isEmpty()) {
            val businessPage1 = neo4jEntityFactory.getByFacebookId(coppiaDiId.first, FacebookBusinessPage::class.java, completeLoad = true) ?: basicInfoGrabber(coppiaDiId.first) as FacebookBusinessPage
            val businessPage2 = neo4jEntityFactory.getByFacebookId(coppiaDiId.second, FacebookBusinessPage::class.java, completeLoad = true) ?: basicInfoGrabber(coppiaDiId.second) as FacebookBusinessPage
            log().info("Add ${likers.size} likers to ${businessPage1.facebookName} and to ${businessPage2.facebookName} from url: $url")
            businessPage1.likers.addAll(likers)
            businessPage2.likers.addAll(likers)
            facebookBusinessPageRepo.save(businessPage1)
            facebookBusinessPageRepo.save(businessPage2)
        }
        File("${coppiaDiId.first}-${coppiaDiId.second}.txt").appendText(url + "\n")
        likers.forEach { File("${coppiaDiId.first}-${coppiaDiId.second}.txt").appendText("facebookName = ${it.facebookName} | facebookId = ${it.facebookId}\n") }
    }


    fun basicInfoGrabber(facebookId: String): FacebookPage {
        val target = targetManger.getOrCreateTargetFromFacebookIdentifer(facebookId)
        return ctx.getBean(BasicInfoGrabber::class.java, target).process()
    }


}