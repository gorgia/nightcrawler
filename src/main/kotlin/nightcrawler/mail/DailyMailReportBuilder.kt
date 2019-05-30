package nightcrawler.mail

import nightcrawler.utils.log
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*
import javax.mail.MessagingException
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart

/**
 * Created by andrea on 10/08/16.
 */
@Component
open class DailyMailReportBuilder {

    companion object {
        var accountProcessedToday = HashSet<String>()
        var timelinesProcessedToday = HashSet<String>()
        var mailSentToday = ArrayList<String>()
    }

    @Scheduled(cron = "0 0 9 1/1 * *")
    fun builReportMail() {
        builReportMail(true)
    }


    fun builReportMail(renitReport: Boolean) {
        var html = "<div id='dailyreport'>" //dailyreport
        html = html + "<h2>DailyReport</h2>\n"
        html = html + "<div id='accountsreport'>" //accountsreport
        html = html + "<h3>Accounts processed today</h3>\n"
        html = html + "<ul>"
        for (s in accountProcessedToday) {
            html = "$html<li>$s</li>"
        }
        html = html + "</ul>"
        html = html + "</div>" //accountsreport
        html = html + "<div id='timlinereport'>" //timelinereport
        html = html + "<h3>Timelines processed today</h3>\n"
        html = html + "<ul>"
        for (s in timelinesProcessedToday) {
            html = "$html<li>$s</li>"
        }
        html = html + "</ul>"
        html = html + "</div>" //timelinereport
        html = html + "<div id='mailsenttoday'>" //mailsenttoday
        html = html + "<h3>Mail sent today</h3>\n"
        html = html + "<ul>"
        for (s in mailSentToday) {
            html = "$html<li>$s</li>"
        }
        html = html + "</ul>"
        html = html + "</div>" //mailsenttoday
        html = html + "</div>" //dailyreport


        val recipients = HashSet<String>(mailSentToday)

        val finalHtmlText = html
        val subject = "Daily report of: " + LocalDate.now()


        recipients.forEach { recipient ->
            try {
                val from = "socialnet@octopus.org"
                val tos = HashSet<String>()
                tos.add(recipient)
                val ccs = HashSet<String>()
                ccs.add("gorgia@fastwebnet.it")
                //ccs.add("webint.italy.001@gmail.com")
                MailSender.sendMail(tos, ccs, from, subject, createMultipart(finalHtmlText))
            } catch (e: MessagingException) {
                log().error("Error during sending of email to: " + recipient, e)
            }
        }

        if (renitReport) {
            reinitReport()
        }
    }


    private fun reinitReport() {
        accountProcessedToday = HashSet<String>()
        timelinesProcessedToday = HashSet<String>()
        mailSentToday = ArrayList<String>()
    }


    @Throws(MessagingException::class, NullPointerException::class)
    private fun createMultipart(htmlText: String): MimeMultipart {
        // This mail has 2 part, the BODY and the embedded image
        val multipart = MimeMultipart("related")

        // first part (the html)
        val messageBodyPart = MimeBodyPart()
        messageBodyPart.setContent(htmlText, "text/html")
        // add it
        multipart.addBodyPart(messageBodyPart)


        // add image to the multipart
        multipart.addBodyPart(messageBodyPart)
        return multipart
    }
}