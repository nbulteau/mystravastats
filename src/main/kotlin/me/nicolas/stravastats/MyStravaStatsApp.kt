package me.nicolas.stravastats

import javafx.scene.image.Image
import javafx.stage.Stage
import me.nicolas.stravastats.ihm.StravaAPIAuthenticationView
import tornadofx.App
import tornadofx.launch
import java.awt.Taskbar
import javax.swing.ImageIcon


class MyStravaStatsApp : App(StravaAPIAuthenticationView::class) {

    override fun start(stage: Stage) {
        stage.isResizable = false
        stage.isAlwaysOnTop = true
        val logoInputStream = javaClass.getResourceAsStream("/images/strava-logo.png")
        stage.icons += Image(logoInputStream)

        val taskbar = Taskbar.getTaskbar()
        try {
            val iconURL = javaClass.getResource("/images/strava-logo.png")
            val image = ImageIcon(iconURL).image
            //set icon for mac os (and other systems which do support this method)
            taskbar.iconImage = image
        } catch (e: UnsupportedOperationException) {
            println("The os does not support: 'taskbar.setIconImage'")
        } catch (e: SecurityException) {
            println("There was a security exception for: 'taskbar.setIconImage'")
        }
        super.start(stage)
    }
}

fun main(args: Array<String>) {
    launch<MyStravaStatsApp>(args)
}