package nightcrawler.database

import java.time.LocalDateTime

/**
 * Created by andrea on 15/09/16.
 */
open class FacebookTarget {
    open var facebookIdentifier: String? = null
    var eagerComments: Boolean = false
    var eagerTimeline: Boolean = false
    var downloadTimeline: Boolean = true
    var saveInDb: Boolean = true
    var doesExist: Boolean = false
    var mailOnCompletion: Boolean = false
    lateinit var updateMailList: MutableSet<String>
    var updateTimelineEveryMinutes: Long = 720
    var updateAttributesEveryDays: Long = 60
    var basicInfoOnly = false
    open var lastUpdateTimeline: LocalDateTime? = null
    open var lastUpdateAttributes: LocalDateTime? = null
}