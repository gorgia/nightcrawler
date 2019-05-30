package nightcrawler.twitter.utils

import nightcrawler.springconfiguration.ApplicationContextProvider
import twitter4j.Status
import twitter4j.Twitter
import java.lang.reflect.Method
import java.time.LocalDateTime
import java.util.*
import java.time.ZoneId
import kotlin.reflect.jvm.internal.impl.serialization.deserialization.ProtoContainer


/**
 * Created by andrea on 31/01/17.
 */


fun getMaxStatusId(statusCollection: Collection<Status>): Long {
    return statusCollection.maxBy { it.id }!!.id
}


fun dateToLocalDateTime(date: Date) : LocalDateTime{
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
}

fun <T> callAndWait(sleepMilliseconds: Long = 60000, function: () -> T): T {
    val result: T = function()
    Thread.sleep(sleepMilliseconds)
    return result
}


fun <T> callOnNewInstance(clazz : Class<out Any>, method: Method, vararg params : Any?) : T{
    return method.invoke(ApplicationContextProvider.ctx?.getBean(clazz), params) as T
}