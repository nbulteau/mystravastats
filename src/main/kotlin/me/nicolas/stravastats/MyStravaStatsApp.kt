package me.nicolas.stravastats

import com.beust.jcommander.JCommander
import javafx.stage.Stage
import me.nicolas.stravastats.ihm.MainView
import tornadofx.App
import tornadofx.launch


class MyStravaStatsApp : App(MainView::class) {

    companion object {
        val myStravaStatsParameters = MyStravaStatsParameters()
    }

    override fun start(stage: Stage) {
        super.start(stage)
        stage.width = 1024.0
        stage.height = 768.0
    }

    override fun init() {
        JCommander.newBuilder()
            .addObject(myStravaStatsParameters)
            .programName("My Strava Stats")
            .build().parse(*parameters.raw.toTypedArray())
    }
}

fun main(args: Array<String>) {
    launch<MyStravaStatsApp>(args)
}