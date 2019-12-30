package nightcrawler.mail

import com.google.common.collect.Multimap
import nightcrawler.TestConfiguration
import nightcrawler.database.neo4j.models.FacebookEntity
import nightcrawler.database.neo4j.models.FacebookPage
import nightcrawler.database.neo4j.models.FacebookPost
import nightcrawler.utils.log
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart


@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(properties = ["scheduling.enabled=false"], classes = [(TestConfiguration::class)])
class MailSenderTest {


    @Test
    fun sendMail() {

        val subject = "this is a test of sending mail"

        //MULTIPART MAIL BODY CREATION
        // This mail has 2 part, the BODY and the embedded image
        val multipart = MimeMultipart("related")

        // first part (the html)
        val mimeBodyPart = MimeBodyPart()

        val htmlText = "<H2>This is a test by Andrea 2018-05</H2><br>"

        mimeBodyPart.setContent(htmlText, "text/html; charset=UTF-8")
        multipart.addBodyPart(mimeBodyPart)

        val recipients = setOf("xxxxx@fastwebnet.it")
        MailSender.sendMail(recipients, emptySet(), "socialnet@xxxxxxx.org", subject, multipart)
    }
}