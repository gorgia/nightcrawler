package nightcrawler.mail

import com.sun.mail.smtp.SMTPTransport
import nightcrawler.utils.conf
import nightcrawler.utils.log
import org.springframework.messaging.MessagingException
import java.security.NoSuchProviderException
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

/**
 * Created by andrea on 10/08/16.
 */
object MailSender {

    val username: String = conf.getString("socialnet.mailsender.username")!!
    val password: String = conf.getString("socialnet.mailsender.password")!!

    @Throws(NoSuchProviderException::class)
    fun sendMail(tos: Set<String>?, ccs: Set<String>?, from: String, subject: String, multipart: MimeMultipart): Boolean {
        // Get system properties
        /*Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", "localhost");*/

        //Security.addProvider(javax.net.ssl.)
        val sslFactory = "javax.net.ssl.SSLSocketFactory"

        val props = Properties()
        props.setProperty("mail.smtps.host", "smtp.gmail.com")
        props.setProperty("mail.smtp.socketFactory.class", sslFactory)
        props.setProperty("mail.smtp.socketFactory.fallback", "false")
        props.setProperty("mail.smtp.port", "465")
        props.setProperty("mail.smtp.socketFactory.port", "465")
        props.setProperty("mail.smtps.auth", "true")

        props.put("mail.smtps.quitwait", "false")

        val session = Session.getInstance(props, null)

        val transport = session.getTransport("smtps") as SMTPTransport

        var everythingok: Boolean
        try {
            // Create a default MimeMessage object.
            val message = MimeMessage(session)
            // Set From: header field of the header.
            message.setFrom(InternetAddress(from))
            // Set To: header field of the header.
            tos?.forEach { to ->
                try {
                    message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                } catch (e: MessagingException) {
                    log().error("unable to parse mail address: $to", e)
                }
            }
            ccs?.forEach { cc ->
                try {
                    message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cc))
                } catch (e: MessagingException) {
                    log().error("unable to parse mail address: $cc", e)
                }
            }
            // Set Subject: header field
            message.subject = subject
            // putInCache everything together
            message.setContent(multipart)
            message.sentDate = Date()
            // Send message
            transport.connect("smtp.gmail.com", username, password)
            transport.sendMessage(message, message.allRecipients)
            transport.close()
            log().info("Sent mail successfully to: " + tos!!)
            everythingok = true
            tos.forEach { to -> DailyMailReportBuilder.mailSentToday.add(to) }
        } catch (e: MessagingException) {
            everythingok = false
            log().error("Impossible to senda mail", e)
        }

        return everythingok
    }


}