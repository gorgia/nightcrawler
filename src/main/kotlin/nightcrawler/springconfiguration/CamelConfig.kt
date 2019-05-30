package nightcrawler.springconfiguration

import nightcrawler.utils.conf
import nightcrawler.utils.log
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.RedeliveryPolicy
import org.apache.activemq.broker.region.policy.PolicyEntry
import org.apache.activemq.broker.region.policy.SharedDeadLetterStrategy
import org.apache.camel.CamelContext
import org.apache.camel.LoggingLevel
import org.apache.camel.RoutesBuilder
import org.apache.camel.builder.DeadLetterChannelBuilder
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.jms.JmsComponent
import org.apache.camel.processor.exceptionpolicy.ExceptionPolicyStrategy
import org.apache.camel.routepolicy.quartz2.SimpleScheduledRoutePolicy
import org.apache.camel.spi.RoutePolicy
import org.apache.camel.spi.RoutePolicyFactory
import org.apache.camel.spring.SpringCamelContext
import org.apache.camel.spring.javaconfig.CamelConfiguration
import org.apache.camel.support.RoutePolicySupport
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

/**
 * Created by andrea on 08/07/16.
 */
@Configuration
//@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
class CamelConfig : CamelConfiguration(), InitializingBean {
    override fun afterPropertiesSet() {
        log().info("CamelConfig enabled!")
    }

    /**
     * Returns the CamelContext which support Spring
     */
    @Throws(Exception::class)
    override fun createCamelContext(): CamelContext {
        return SpringCamelContext(applicationContext)
    }

    @Throws(Exception::class)
    override fun setupCamelContext(camelContext: CamelContext?) {
        // setup the ActiveMQ component
        val connectionFactory = ActiveMQConnectionFactory(conf.getString("socialnet.activemq.brokerUrl"))
        connectionFactory.isTrustAllPackages = true
        val connectionFactoryAntonio = ActiveMQConnectionFactory(conf.getString("socialnet.activemq.brokerUrlAntonio"))
        // and register it into the CamelContext
        val answerAntonio = JmsComponent()
        answerAntonio.setConnectionFactory(connectionFactoryAntonio)
        camelContext!!.addComponent("antonio", answerAntonio)

        // and register it into the CamelContext
        val answerAndrea = JmsComponent()
        answerAndrea.setConnectionFactory(connectionFactory)
        camelContext.addComponent("jms", answerAndrea)
        camelContext.isAllowUseOriginalMessage = false
        camelContext.shutdownStrategy.timeout = Long.MAX_VALUE
        camelContext.shutdownStrategy.isSuppressLoggingOnTimeout = true

    }


    @Bean
    fun myRouter(): RoutesBuilder {
        log().info("Starting Camel Rutes")
        return object : RouteBuilder() {
            @Throws(Exception::class)
            override fun configure() {
                val policy = SimpleScheduledRoutePolicy()
                val delayedTime = System.currentTimeMillis() + 30000L
                policy.routeStartDate = Date(delayedTime)



                val sharesUrlConcurrentConsumers = if (conf.hasPath("socialnet.sharesUrlConcurrentConsumers")) conf.getInt("socialnet.sharesUrlConcurrentConsumers") else 2
                if (sharesUrlConcurrentConsumers > 0)
                    from("jms:queue:sharesUrl?concurrentConsumers=$sharesUrlConcurrentConsumers").autoStartup(false).routePolicy(policy).process("postSharesGrabber")
                val likesUrlConcurrentConsumers = if (conf.hasPath("socialnet.likesUrlConcurrentConsumers")) conf.getInt("socialnet.likesUrlConcurrentConsumers") else 3
                if (likesUrlConcurrentConsumers > 0)
                    from("jms:queue:likesUrl?concurrentConsumers=$likesUrlConcurrentConsumers").autoStartup(false).routePolicy(policy).process("postLikesGrabber")
                from("jms:queue:ActiveMQ.DLQ").to("mock:nothing")

            }
        }
    }
}