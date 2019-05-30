package nightcrawler.crawler.webdriver

import java.util.*

/**
 * Created by andrea on 27/07/16.
 */
class Cookie : org.openqa.selenium.Cookie {
    constructor(name: String, value: String, path: String, expiry: Date) : super(name, value, path, expiry) {
    }

    constructor(name: String, value: String, domain: String, path: String, expiry: Date) : super(name, value, domain, path, expiry) {
    }

    constructor(name: String, value: String, domain: String, path: String, expiry: Date, isSecure: Boolean) : super(name, value, domain, path, expiry, isSecure) {
    }

    constructor(name: String, value: String, domain: String, path: String, expiry: Date, isSecure: Boolean, isHttpOnly: Boolean) : super(name, value, domain, path, expiry, isSecure, isHttpOnly) {
    }

    constructor(name: String, value: String) : super(name, value) {
    }

    constructor(name: String, value: String, path: String) : super(name, value, path) {
    }

    constructor() : super("none", "none") {
    }

    constructor(cookie: org.openqa.selenium.Cookie) : super(cookie.name, cookie.value, cookie.path, cookie.expiry) {
    }
}
