package nightcrawler.database.neo4j

import com.hazelcast.nio.serialization.SerializableByConvention
import nightcrawler.database.neo4j.models.FacebookEntity
import org.springframework.data.neo4j.repository.Neo4jRepository
import java.io.Serializable

/**
 * Created by andrea on 18/08/16.
 */
interface FindByFacebookId<out T : FacebookEntity> {
    fun findByFacebookId(facebookId: String): T?
}