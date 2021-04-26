package me.nicolas.stravastats

import com.beust.jcommander.JCommander
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import me.nicolas.stravastats.core.*
import me.nicolas.stravastats.core.ActivityService
import me.nicolas.stravastats.core.StatisticsService
import me.nicolas.stravastats.strava.StravaApi


internal class MyStravaStats(incomingArgs: Array<String>) {

    private val stravaStatsProperties = loadPropertiesFromFile()

    private val stravaApi = StravaApi(stravaStatsProperties)

    private val activityLoader = ActivityService(stravaStatsProperties, stravaApi)

    private val statsBuilder = StatisticsService()

    private val chartsBuilder = ChartsService()

    private val csvExporter = CSVService()

    private val parameters = Parameters()

    init {
        JCommander.newBuilder()
            .addObject(parameters)
            .programName("My Strava Stats")
            .build().parse(*incomingArgs)
    }

    fun run() {
        val startTime = System.currentTimeMillis()

        val activities = activityLoader.loadActivities(parameters.clientId, parameters.clientSecret, parameters.year)

        if (stravaStatsProperties.removingNonMovingSections) {
            activities.forEach { activity -> activity.removeNonMoving() }
        }

        val stravaStats = statsBuilder.computeStatistics(activities)
        println(stravaStats)

        if (parameters.csv) {
            csvExporter.exportCSV(parameters.clientId, activities, parameters.filter)
        }

        if (parameters.charts) {
            chartsBuilder.buildCharts(activities)
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
    MyStravaStats(incomingArgs).run()
}






