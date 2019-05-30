package nightcrawler.springconfiguration

import com.mongodb.MongoClient
import nightcrawler.utils.conf
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoTypeMapper




/**
 * Created by andrea on 26/07/16.
 */
@Configuration
@EnableMongoRepositories(basePackages = ["nightcrawler.database.mongodb.repositories", "nightcrawler.twitter.databases.mongo.repo"])
class MongoConfig {

    @Bean
    fun mongoClient(): MongoClient {
        return MongoClient(conf.getString("socialnet.mongo.serveraddress"))
    }

    @Bean
    fun mongoTemplate(): MongoTemplate {
        return MongoTemplate(mongoClient(), "test")
    }

}