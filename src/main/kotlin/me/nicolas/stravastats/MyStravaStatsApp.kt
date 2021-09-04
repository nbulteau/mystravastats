package me.nicolas.stravastats

import javafx.scene.image.Image
import javafx.stage.Stage
import me.nicolas.stravastats.ihm.StravaAPIAuthenticationView
import sun.net.www.protocol.css.Handler
import tornadofx.*
import java.awt.Taskbar
import java.net.URL
import javax.swing.ImageIcon


internal class MyStravaStatsApp : App(StravaAPIAuthenticationView::class) {

    override fun start(stage: Stage) {
        stage.isResizable = false
        stage.isAlwaysOnTop = true
        val logoInputStream = javaClass.getResourceAsStream("/images/strava-logo.png")
        stage.icons += Image(logoInputStream)

        if (OSValidator.IS_WINDOWS || OSValidator.IS_MAC) {
            val taskbar = Taskbar.getTaskbar()
            val iconURL = javaClass.getResource("/images/strava-logo.png")
            taskbar.iconImage = ImageIcon(iconURL).image
        }

        super.start(stage)
    }
}

fun main(args: Array<String>) {
    removeJavaFxInfoMessage()
    launch<MyStravaStatsApp>(args)
}

