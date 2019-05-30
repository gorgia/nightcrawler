package nightcrawler.crawler.webdriver

import org.openqa.selenium.WebElement
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.internal.WrapsDriver
import org.openqa.selenium.remote.RemoteWebElement


/**
 * Created by andrea on 03/01/17.
 */
/*
fun WebElement.setAttribute(attr : String, value:String?){
    val driver = (this as WrapsDriver).wrappedDriver
    val js = driver as JavascriptExecutor
    js.executeScript("document.getElementById('${this.getAttribute("id")}').setAttribute('$attr', '$value')")
}
*/