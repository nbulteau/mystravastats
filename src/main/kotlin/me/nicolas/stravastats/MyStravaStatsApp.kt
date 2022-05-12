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
import kotlin.reflect.KClass


internal class MyStravaStatsApp : App() {

    override lateinit var primaryView: KClass<out UIComponent>

    override fun start(stage: Stage) {
        stage.isResizable = false
        stage.isAlwaysOnTop = true
        setIcons(stage)

        // Add feed mode to userdata (FIT files or STRAVA)
        stage.userData = if (parameters.raw.isNotEmpty() && parameters.raw[0] == "FIT") {
            primaryView = SplashScreenView::class
            "FIT"
        } else {
            primaryView = StravaAPIAuthenticationView::class
            "STRAVA"
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

