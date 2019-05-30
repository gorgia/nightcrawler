package nightcrawler.utils


import org.junit.Test

class StringMatcherTest {


    @Test
    open fun simpleTest() {
        val reference = "permanentblock"
        val toMatch = "string that contains 'real identity'"
        kotlin.test.assertTrue {  StringMatcher.containsAny(toMatch, reference) }
    }
}