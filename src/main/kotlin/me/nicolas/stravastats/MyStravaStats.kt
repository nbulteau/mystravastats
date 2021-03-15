package me.nicolas.stravastats

import com.beust.jcommander.JCommander
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.core.ActivityLoader
import me.nicolas.stravastats.core.StatsBuilder
import me.nicolas.stravastats.core.StravaService
import me.nicolas.stravastats.strava.StravaApi
import java.time.LocalDate


internal class MyStravaStats(incomingArgs: Array<String>) {

    // build instances
    private val stravaStatsProperties = loadPropertiesFromFile()

    private val stravaApi = StravaApi(stravaStatsProperties)

    private val statsBuilder = StatsBuilder()

    private val activityLoader = ActivityLoader(stravaStatsProperties, stravaApi)

    private val stravaService = StravaService(statsBuilder)

    private val parameters = Parameters()

    init {
        JCommander.newBuilder()
            .addObject(parameters)
            .programName("My Strava Stats")
            .build().parse(*incomingArgs)

        println("http://www.strava.com/api/v3/oauth/authorize?client_id=${parameters.clientId}&response_type=code&redirect_uri=http://localhost:8080/exchange_token&approval_prompt=auto&scope=read_all,activity:read_all")
    }

    fun run() {
        val startTime = System.currentTimeMillis()

        val activities = loadActivities()

        if (stravaStatsProperties.removingNonMovingSections) {
            removeNonMovingSections(activities)
        }

        displayStatistics(activities)

        if (parameters.csv) {
            exportCSV(activities)
        }

        exportCharts(activities)

        println()
        println("Execution time = ${System.currentTimeMillis() - startTime} m")
    }

    private fun exportCharts(activities: List<Activity>) {

    }

    private fun exportCSV(activities: List<Activity>) {
        activities
            .groupBy { activity -> activity.startDateLocal.subSequence(0, 4).toString() } // year by year
            .forEach { exportCSV(filterActivities(it.value), it.key.toInt()) }
    }

    private fun removeNonMovingSections(activities: List<Activity>) {
        activities.forEach { activity -> activity.removeNonMoving() }
    }

    private fun loadActivities(): MutableList<Activity> {
        val activities = mutableListOf<Activity>()
        if (parameters.year != null) {
            activities.addAll(
                activityLoader.loadActivities(
                    parameters.clientId,
                    parameters.year!!,
                    parameters.accessToken,
                    parameters.clientSecret,
                    parameters.code
                )
            )
        } else {
            for (year in LocalDate.now().year downTo 2010) {
                activities.addAll(
                    activityLoader.loadActivities(
                        parameters.clientId,
                        year,
                        parameters.accessToken,
                        parameters.clientSecret,
                        parameters.code
                    )
                )
            }
        }
        return activities
    }

    /**
     * Display statistics
     */
    private fun displayStatistics(activities: List<Activity>) {
        val stravaStats = stravaService.computeStatistics(activities)
        stravaStats.displayStatistics()
    }

    /**
     * Export activities in a CSV file.
     */
    private fun exportCSV(activities: List<Activity>, year: Int) {
        print("* Export activities for $year [")
        print("Ride")
        stravaService.exportBikeCSV(activities.filter { activity -> activity.type == "Ride" }, "Ride", year)
        print(", Run")
        stravaService.exportRunCSV(activities.filter { activity -> activity.type == "Run" }, "Run", year)
        print(", Hike")
        stravaService.exportHikeCSV(activities.filter { activity -> activity.type == "Hike" }, "Hike", year)
        print(", InlineSkate")
        stravaService.exportHikeCSV(
            activities.filter { activity -> activity.type == "InlineSkate" },
            "InlineSkate",
            year
        )
        println("]")

    }

    /**
     * Apply filter if exist.
     * @param activities activities to filter.
     */
    private fun filterActivities(activities: List<Activity>): List<Activity> {
        return if (parameters.filter != null) {
            val lowBoundary = parameters.filter!! - (5 * parameters.filter!! / 100)
            val highBoundary = parameters.filter!! + (5 * parameters.filter!! / 100)

            activities.filter { activity -> activity.distance > lowBoundary && activity.distance < highBoundary }
        } else {
            activities
        }
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






