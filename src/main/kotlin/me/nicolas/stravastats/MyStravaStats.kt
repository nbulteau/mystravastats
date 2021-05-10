package me.nicolas.stravastats

import com.beust.jcommander.JCommander
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import me.nicolas.stravastats.business.StravaStatistics
import me.nicolas.stravastats.service.StravaService
import me.nicolas.stravastats.service.CSVService
import me.nicolas.stravastats.service.ChartsService
import me.nicolas.stravastats.service.StatisticsService
import me.nicolas.stravastats.strava.StravaApi


internal class MyStravaStats(incomingArgs: Array<String>) {

    private val myStravaStatsProperties = loadPropertiesFromFile()

    private val stravaService = StravaService(StravaApi(myStravaStatsProperties))

    private val statisticsService = StatisticsService()

    private val chartsService = ChartsService()

    private val csvService = CSVService()

    private val myStravaStatsParameters = MyStravaStatsParameters()

    init {
        JCommander.newBuilder()
            .addObject(myStravaStatsParameters)
            .programName("My Strava Stats")
            .build().parse(*incomingArgs)
    }

    fun run() {
        val startTime = System.currentTimeMillis()

        val activities = stravaService.loadActivities(myStravaStatsParameters.clientId, myStravaStatsParameters.clientSecret, myStravaStatsParameters.year)

        if (myStravaStatsProperties.removingNonMovingSections) {
            activities.forEach { activity -> activity.removeNonMoving() }
        }

        val stravaStats: StravaStatistics = statisticsService.computeStatistics(activities)
        println("Nb activities used to compute statistics (with streams) : ${activities.size}")
        println(stravaStats)

        if (myStravaStatsParameters.csv) {
            csvService.exportCSV(myStravaStatsParameters.clientId, activities, myStravaStatsParameters.filter)
        }

        if (myStravaStatsParameters.charts) {
            chartsService.buildCharts(activities)
        }

        println()
        println("Execution time = ${System.currentTimeMillis() - startTime} ms")
    }

    /**
     * Load properties from application.yml
     */
    private fun loadPropertiesFromFile(): MyStravaStatsProperties {

        val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
        mapper.registerModule(KotlinModule()) // Enable Kotlin support

        val inputStream = javaClass.getResourceAsStream("/application.yml")
        return mapper.readValue(inputStream, MyStravaStatsProperties::class.java)
    }
}

private fun disableWarning() {
    System.err.close()
    System.setErr(System.out)
}

fun main(incomingArgs: Array<String>) {
    disableWarning()
    try {
        MyStravaStats(incomingArgs).run()
    }  catch (throwable: Throwable) {
        println("\n${throwable.message ?: ""}")
    }
}






