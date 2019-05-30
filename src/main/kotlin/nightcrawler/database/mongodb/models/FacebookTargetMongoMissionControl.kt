package nightcrawler.database.mongodb.models

import java.io.Serializable
import java.time.LocalDateTime

data class FacebookTargetMongoMissionControl(var missionName: String? = null,
                                             var depth: Int = 0,
                                             var parentTargetId: String? = null,
                                             var priority: Int = 0,
                                             var saveDerivedAsTarget: Boolean = false,
                                             var lastUpdate: LocalDateTime? = null,
                                             var updateEveryDays: Long = 3600) : Serializable {
}