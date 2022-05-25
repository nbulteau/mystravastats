package me.nicolas.stravastats

import javafx.scene.image.Image
import javafx.stage.Stage
import me.nicolas.stravastats.ihm.SplashScreenView
import me.nicolas.stravastats.ihm.StravaAPIAuthenticationView
import tornadofx.App
import tornadofx.UIComponent
import tornadofx.launch
import java.awt.Taskbar
import javax.swing.ImageIcon
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.reflect.KClass


internal class MyStravaStatsApp : App() {

    override lateinit var primaryView: KClass<out UIComponent>

    override fun start(stage: Stage) {
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
        val logoInputStream = javaClass.getResourceAsStream("/images/strava-logo.png")
        stage.icons += Image(logoInputStream)

        if (OSValidator.IS_MAC) {
            val taskbar = Taskbar.getTaskbar()
            val iconURL = javaClass.getResource("/images/strava-logo.png")
            taskbar.iconImage = ImageIcon(iconURL).image
        }
    }
}

fun main(args: Array<String>) {
    removeJavaFxInfoMessage()
    launch<MyStravaStatsApp>(args)
}

