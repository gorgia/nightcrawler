package nightcrawler.utils

/**
 * Created by andrea on 07/06/16.
 */
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

// Based on answer by Jayson Minard on Stack Oveflow question
// "Idiomatic logging for Kotlin"
// See: http://stackoverflow.com/questions/34416869

// Return logger for Java class, if companion object fix the name
public fun <T : Any> log(forClass: Class<T>): Logger {
    return LoggerFactory.getLogger(unwrapCompanionClass(forClass).name)
}

// unwrap companion class to enclosing class given a Java Class
public fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return if (ofClass.enclosingClass != null &&
            ofClass.enclosingClass.kotlin.objectInstance?.javaClass == ofClass) {
        ofClass.enclosingClass
    } else {
        ofClass
    }
}

// unwrap companion class to enclosing class given a Kotlin Class
public fun <T : Any> unwrapCompanionClass(ofClass: KClass<T>): KClass<*> {
    return unwrapCompanionClass(ofClass.java).kotlin
}

// Return logger for Kotlin class
public fun <T : Any> log(forClass: KClass<T>): Logger {
    return log(forClass.java)
}

// return logger from extended class (or the enclosing class)
public fun <T : Any> T.log(): Logger {
    return log(this.javaClass)
}

// return a lazy logger property delegate for enclosing class
public fun <R : Any> R.lazyLogger(): Lazy<Logger> {
    return lazy { log(this.javaClass) }
}

// return a logger property delegate for enclosing class
public fun <R : Any> R.injectLogger(): Lazy<Logger> {
    return lazyOf(log(this.javaClass))
}

// marker interface and related extension (remove extension for Any.log() in
// favour of this)
interface Loggable {}

public fun Loggable.log(): Logger = log(this.javaClass)

// abstract base class to provide logging, intended for companion objects more
// than classes but works for either
public abstract class WithLogging : Loggable {
    val log = log()
}