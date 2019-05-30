package nightcrawler.crawler.webdriver


import nightcrawler.utils.conf
import nightcrawler.utils.log
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.FirefoxProfile
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.phantomjs.PhantomJSDriverService
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import java.net.MalformedURLException
import java.net.URL
import java.util.*


/**
 * Created by andrea on 01/04/16.
 */

object WebDriverFactory {


    private val pageLoadingTimeSeconds = conf.getLong("socialnet.webDriver.pageLoadingMaxTime")
    private val browserType = conf.getString("socialnet.webDriver.browserType")
    private val seleniumHubGridServerAddress = conf.getString("socialnet.webDriver.seleniumHubGridServerAddress")


    fun create(browserType: String = this.browserType, pageLoadingTimeSeconds: Long = this.pageLoadingTimeSeconds, proxy: String? = null): WebDriver {
        log().debug("Create webDriver with params: browserType $browserType, pageLoadingTimeSeconds $pageLoadingTimeSeconds, proxy: $proxy")
        val webDriver: WebDriver
        val capabilities: MutableCapabilities = when (browserType) {
            "chrome" -> initChromeBrowser()
            "firefox" -> initFirefoxBrowser()
            "remote-firefox" -> initRemoteFirefox()
            "remote-chrome" -> initRemoteChrome()
            else -> initFirefoxBrowser()
        }

        if (proxy != null) setProxy(capabilities)

        webDriver = when (browserType) {
            "chrome" -> ChromeDriver(capabilities as ChromeOptions)
            "firefox" -> FirefoxDriver(capabilities as FirefoxOptions)
            "remote-firefox" -> retrieveBrowserFromRemoteGrid(capabilities) ?: FirefoxDriver(capabilities as FirefoxOptions)
            "remote-chrome" -> retrieveBrowserFromRemoteGrid(capabilities) ?: FirefoxDriver(capabilities as FirefoxOptions)
            else -> {
                HtmlUnitDriver()
            }
        }
        log().info("Created webdriver with fingerprint: $webDriver")
        return webDriver
    }


    private fun setProxy(capabilities: MutableCapabilities, proxyAddress: String? = null) {
        if (proxyAddress == null) return
        val proxy = Proxy()
        proxy.ftpProxy = proxyAddress
        proxy.httpProxy = proxyAddress
        proxy.sslProxy = proxyAddress
        capabilities.setCapability(CapabilityType.PROXY, proxy)
    }

    private fun initRemoteFirefox(loadImages: Boolean = false): DesiredCapabilities {
        log().info("Init remote Firefox WebBrowser")
        val capabilities = DesiredCapabilities.firefox()
        capabilities.setCapability("web-security", false)
        capabilities.setCapability("ssl-protocol", "any")
        capabilities.setCapability("ignore-ssl-errors", true)
        capabilities.setCapability("unexpectedAlertBehaviour", "ignore")
        capabilities.setCapability("webdriver.remote.quietExceptions", true)
        capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE)
        capabilities.isJavascriptEnabled = true
        val firefoxProfile = FirefoxProfile()
        firefoxProfile.setAlwaysLoadNoFocusLib(true)
        firefoxProfile.setAcceptUntrustedCertificates(true)
        if (!loadImages) {
            firefoxProfile.setPreference("permissions.default.image", 2)
        }
        capabilities.setCapability(FirefoxDriver.PROFILE, firefoxProfile)
        return capabilities
    }


    private fun initFirefoxBrowser(): FirefoxOptions {
        log().info("Init Firefox WebBrowser")
        val customProfile = FirefoxProfile()
        customProfile.setAcceptUntrustedCertificates(true)
        customProfile.setPreference("network.http.connection-timeout", 100)
        customProfile.setPreference("network.http.connection-retry-timeout", 100)
        val capabilities = FirefoxOptions() //MutableCapabilities.firefox()
        capabilities.setCapability(FirefoxDriver.PROFILE, customProfile)
        return capabilities
    }

    private fun initChromeBrowser(): ChromeOptions {
        log().info("Init Chrome WebBrowser")
        val options = ChromeOptions()
        options.setBinary("/opt/google/chrome/google-chrome")
        System.setProperty("webDriver.chrome.driver", "/opt/google/chrome/chromedriver")
        options.setCapability(ChromeOptions.CAPABILITY, options)
        return options

    }


    private fun initRemoteChrome(): DesiredCapabilities {
        log().info("Init remote Chrome WebBrowser")
        val capabilities = DesiredCapabilities.chrome()
        capabilities.isJavascriptEnabled = true
        //Create prefs map to store all preferences
        val prefs = HashMap<String, Any>()
        //Put this into prefs map to switch off browser notification
        prefs.put("profile.default_content_setting_values.notifications", 2)
        //Create chrome options to set this prefs
        val options = ChromeOptions()
        options.setExperimentalOption("prefs", prefs)
        capabilities.setCapability(ChromeOptions.CAPABILITY, options)
        return capabilities
    }

    private fun retrieveBrowserFromRemoteGrid(capabilities: MutableCapabilities, seleniumHubGridServerAddress: String = this.seleniumHubGridServerAddress): WebDriver? {
        var webDriver: WebDriver? = null
        try {
            webDriver = RemoteWebDriver(URL(seleniumHubGridServerAddress), capabilities)
        } catch (e: MalformedURLException) {
            log().error("Selenium Hub is not active. Recover with local PhantomJS driver", e)
        } catch (e: InterruptedException) {
            log().error("", e)
        } catch (e: Exception) {
            log().error("", e)
        }
        return webDriver
    }

}

