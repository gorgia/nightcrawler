package nightcrawler.database.neo4j

import org.neo4j.ogm.typeconversion.AttributeConverter
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * Created by andrea on 16/09/16.
 */
class LocalDateTimeConverter : AttributeConverter<LocalDateTime, Long> {
    override fun toEntityAttribute(value: Long?): LocalDateTime? {
        if (value == null) return null
        return LocalDateTime.ofInstant(Date(value).toInstant(), ZoneId.systemDefault())
    }

    override fun toGraphProperty(value: LocalDateTime?): Long? {
        if (value == null) return null
        val instant = value.atZone(ZoneId.systemDefault()).toInstant()
        return Date.from(instant).time
    }


}

