package nightcrawler.springconfiguration

import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Created by andrea on 26/07/16.
 */
@Configuration
class HazelcastConfig {


    @Bean
    fun config(): Config {// Set up any non-default config here
        val config = Config()
        config.networkConfig.join.multicastConfig.isEnabled = false
        return config
    }

    fun hazelcastInstance(): HazelcastInstance {
        return Hazelcast.newHazelcastInstance(config())
    }


}