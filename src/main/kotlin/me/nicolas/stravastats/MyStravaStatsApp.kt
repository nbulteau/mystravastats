package me.nicolas.stravastats

import javafx.scene.image.Image
import javafx.stage.Stage
import me.nicolas.stravastats.ihm.StravaAPIAuthenticationView
import tornadofx.App
import tornadofx.launch
import java.awt.Taskbar
import java.util.*
import javax.swing.ImageIcon

internal class OSValidator {
    companion object {
        private val OS = System.getProperty("os.name").lowercase(Locale.getDefault())
        var IS_WINDOWS = OS.indexOf("win") >= 0
        var IS_MAC = OS.indexOf("mac") >= 0
        var IS_UNIX = OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0
        var IS_SOLARIS = OS.indexOf("sunos") >= 0
    }
}

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
    launch<MyStravaStatsApp>(args)
}