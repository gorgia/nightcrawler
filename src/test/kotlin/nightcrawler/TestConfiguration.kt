package nightcrawler

import nightcrawler.springconfiguration.BeanFactoryConfiguration
import nightcrawler.springconfiguration.HazelcastConfig
import nightcrawler.springconfiguration.MongoConfig
import nightcrawler.springconfiguration.Neo4jConfig
import nightcrawler.twitter.TwitterConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import

/**
 * Created by andrea on 24/08/16.
 */


@SpringBootApplication(exclude = arrayOf(Neo4jDataAutoConfiguration::class))
@Import(HazelcastConfig::class, MongoConfig::class, Neo4jConfig::class, BeanFactoryConfiguration::class, TwitterConfig::class)
@ComponentScan(basePackages = arrayOf("nightcrawler"),
        excludeFilters = arrayOf(ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = arrayOf("nightcrawler.springconfiguration", "nightcrawler.ApplicationNightcrawler")
        ))
)
open class TestConfiguration{

}