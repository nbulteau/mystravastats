package me.nicolas.stravastats.utils

import sun.net.www.protocol.css.Handler
import java.awt.Desktop
import java.net.URI
import java.net.URL
import java.util.*


internal class OSValidator {
    companion object {
        private val OS = System.getProperty("os.name").lowercase(Locale.getDefault())
        var IS_WINDOWS = OS.indexOf("win") >= 0
        var IS_MAC = OS.indexOf("mac") >= 0
        var IS_UNIX = OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0
    }
}

/**
 * Remove INFO message
 */
fun removeJavaFxInfoMessage() {
    URL.setURLStreamHandlerFactory(Handler.HandlerFactory())
}

/**
 * Open Browser
 */
fun openBrowser(url: String) {
    val runtime = Runtime.getRuntime()
    when {
        OSValidator.IS_WINDOWS -> {
            runtime.exec("rundll32 url.dll,FileProtocolHandler $url")
        }
        OSValidator.IS_MAC -> {
            val uri = if (url.startsWith("http")) {
                url
            } else {
                "file://$url"
            }
            Desktop.getDesktop().browse(URI(uri))
        }
        OSValidator.IS_UNIX -> {
            runtime.exec("xdg-open $url")
        }
    }
}