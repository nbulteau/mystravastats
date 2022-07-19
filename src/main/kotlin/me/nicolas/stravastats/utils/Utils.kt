package me.nicolas.stravastats.utils

import sun.net.www.protocol.css.Handler
import java.net.URL

/**
 * Remove INFO message
 */
fun removeJavaFxInfoMessage() {
    URL.setURLStreamHandlerFactory(Handler.HandlerFactory())
}

