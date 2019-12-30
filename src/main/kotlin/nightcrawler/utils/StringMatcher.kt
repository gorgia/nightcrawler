package nightcrawler.utils

import java.io.InputStream

object StringMatcher {
    private val stringReference  = HashMap<String, Set<String>>()

    init {
        val inputStream: InputStream = StringMatcher::class.java.classLoader.getResourceAsStream("stringreference.txt")
        val lineList = mutableListOf<String>()
        inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it)} }
        lineList.forEach{
            val stringSet : MutableSet<String> = HashSet()
            val keyvaluesplit = it.split("=")
            val key = keyvaluesplit[0].trim()
            keyvaluesplit[1].split("|").forEach {stringSet.add(it.trim())}
            stringReference.put(key, stringSet)
        }
    }


    fun containsAny(toMatch: String, againstRef: String): Boolean {
        val referencedSet = StringMatcher.stringReference[againstRef]
        return referencedSet?.any {
            toMatch.contains(it)
        } ?: false
    }
}