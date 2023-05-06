package me.nicolas.stravastats

import javafx.application.HostServices
import javafx.scene.image.Image
import javafx.stage.Stage
import me.nicolas.stravastats.ihm.SplashScreenView
import me.nicolas.stravastats.ihm.StravaAPIAuthenticationView
import me.nicolas.stravastats.utils.removeJavaFxInfoMessage
import tornadofx.App
import tornadofx.UIComponent
import tornadofx.launch
import java.awt.Taskbar
import java.util.*
import javax.swing.ImageIcon
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.reflect.KClass


internal class MyStravaStatsApp : App() {

    companion object {
        // This is a miserably ugly solution
        private lateinit var hostServices: HostServices

        fun openBrowser(url: String) {
            hostServices.showDocument(url)
        }

        private val OS = System.getProperty("os.name").lowercase(Locale.getDefault())
        val IS_WINDOWS = OS.indexOf("win") >= 0
        val IS_MAC = OS.indexOf("mac") >= 0
        val IS_UNIX = OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0

    }

    override lateinit var primaryView: KClass<out UIComponent>

    override fun start(stage: Stage) {
        MyStravaStatsApp.hostServices = hostServices

        stage.isResizable = false
        setIcons(stage)


        // Add feed mode to userdata (FIT files or STRAVA)
        if (parameters.raw.isNotEmpty() && parameters.raw[0] == "FIT") {
            val pathStr = parameters.raw.getOrElse(1) { "" }
            val cachePath = Path(pathStr)
            if (!cachePath.exists()) {
                println("$cachePath does not exist")
                throw RuntimeException("$cachePath does not exist")
            }
            val clientId = if (pathStr.isNotEmpty()) {
                pathStr.substringAfterLast("fit-")
            } else {
                "xxxxx"
            }

            primaryView = SplashScreenView::class
            stage.userData = mapOf("type" to "FIT", "path" to cachePath, "clientId" to clientId)
        } else {
            primaryView = StravaAPIAuthenticationView::class
            stage.userData = mapOf("type" to "STRAVA")
        }

        super.start(stage)
    }

    private fun setIcons(stage: Stage) {
        if (IS_MAC) {
            val taskbar = Taskbar.getTaskbar()
            val iconURL = javaClass.getResource("/images/strava-logo.png")
            taskbar.iconImage = ImageIcon(iconURL).image
        } else {
            val logoInputStream = javaClass.getResourceAsStream("/images/strava-logo.png")
            stage.icons += Image(logoInputStream)
        }
    }
}

fun main(args: Array<String>) {
    removeJavaFxInfoMessage()
    launch<MyStravaStatsApp>(args)
}

