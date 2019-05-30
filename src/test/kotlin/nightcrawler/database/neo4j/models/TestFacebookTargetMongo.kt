package nightcrawler.database.neo4j.models

import nightcrawler.TestConfiguration
import nightcrawler.database.mongodb.repositories.FacebookTargetRepository
import nightcrawler.utils.log
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * Created by andrea on 26/04/17.
 */

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = arrayOf("scheduling.enabled=false"), classes = arrayOf(TestConfiguration::class))
class TestFacebookTargetMongo {
    @Autowired
    lateinit var facebookTargetRepo : FacebookTargetRepository

    @Test
    @Ignore
    open fun cleanWithoutMail(){
        facebookTargetRepo.findAll().forEach { if(it.updateMailList?.isEmpty()?:true){
            it.eagerComments = false
            it.eagerTimeline = false
        it.updateAttributesEveryDays = 90
        it.updateTimelineEveryMinutes = 129600
        facebookTargetRepo.save(it)
        } }
    }

    @Test
    open fun getPetrastranaTargets(){
        facebookTargetRepo.findAll().filter { it.updateMailList?.contains("petrastrana@gmail.com")?:false}.forEach { log().info(it.facebookIdentifier + " | " + it.neo4jNodeId.toString()) }
    }

}