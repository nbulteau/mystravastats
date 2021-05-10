package me.nicolas.stravastats

import com.beust.jcommander.JCommander
import javafx.stage.Stage
import me.nicolas.stravastats.ihm.SplashScreenView
import tornadofx.App
import tornadofx.launch


internal class MyStravaStatsApp : App(SplashScreenView::class) {

    companion object {
        val myStravaStatsParameters = MyStravaStatsParameters()
    }

    override fun init() {
        JCommander.newBuilder()
            .addObject(myStravaStatsParameters)
            .programName("My Strava Stats")
            .build().parse(*parameters.raw.toTypedArray())
    }

    override fun start(stage: Stage) {
        stage.width = 1024.0
        super.start(stage)
    }
}

fun main(args: Array<String>) {
    launch<MyStravaStatsApp>(args)
}