package me.nicolas.stravastats

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import me.nicolas.stravastats.core.ActivityLoader
import me.nicolas.stravastats.core.StatsBuilder
import me.nicolas.stravastats.core.StravaService
import me.nicolas.stravastats.core.business.formatDate
import me.nicolas.stravastats.core.business.formatSeconds
import me.nicolas.stravastats.infrastructure.StravaApi
import me.nicolas.stravastats.infrastructure.dao.Activity

internal class MyStravaStats(incomingArgs: Array<String>) {

    companion object {
        const val DATE_PAD = 34
        const val DISTANCE_PAD = 10
        const val TIME_PAD = 14
    }

    private val stravaStatsProperties = loadPropertiesFromFile()

    private val stravaApi = StravaApi(stravaStatsProperties)

    private val statsBuilder = StatsBuilder()

    private val activityLoader = ActivityLoader(stravaStatsProperties, stravaApi)

    private val stravaService = StravaService(statsBuilder)

    private val parameters = Parameters()

    init {
        JCommander.newBuilder()
            .addObject(parameters)
            .programName("Strava Stats")
            .build().parse(*incomingArgs)

        println("http://www.strava.com/api/v3/oauth/authorize?client_id=${parameters.clientId}&response_type=code&redirect_uri=http://localhost:8080/exchange_token&approval_prompt=auto&scope=read_all,activity:read_all")
    }

    fun run() {
        val startTime = System.currentTimeMillis()

        val activities = loadActivities()

        if (stravaStatsProperties.removingNonMovingSections) {
            activities.forEach { it.removeNonMoving() }
        }

        displayStatistics(activities)
        if (parameters.displayActivities) {
            displayActivities(activities)
        }

        println()
        println("Execution time = ${System.currentTimeMillis() - startTime} ms")
    }

    /**
     * Display statistics
     */
    private fun displayStatistics(activities: List<Activity>) {
        val stravaStats = stravaService.computeStatistics(activities)
        stravaStats.displayStatistics()
    }

    /**
     * Display activities
     */
    private fun displayActivities(activities: List<Activity>) {
        println("* Activities")
        val filteredActivities = filterActivities(activities)

        println("** Rides")
        doDisplayActivities(filteredActivities.filter { it.type == "Ride" })
        println("** Run")
        doDisplayActivities(filteredActivities.filter { it.type == "Run" })
        println("** Hike")
        doDisplayActivities(filteredActivities.filter { it.type == "Hike" })
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
     * Display activities.
     * @param activities activities to display.
     */
    private fun doDisplayActivities(activities: List<Activity>) {

        // if no activities : nothing to do
        if (activities.isEmpty()) {
            return
        }

        // list all activities
        val maxActivityNameLength = activities.maxByOrNull { it.name.length }?.name?.length!!

        println(
            "Date".padEnd(DATE_PAD)
                    + "Description".padEnd(maxActivityNameLength + 1)
                    + "Distance".padEnd(DISTANCE_PAD)
                    + "Time".padEnd(TIME_PAD)
                    + "Speed"
        )
        activities.forEach { activity ->
            println(
                activity.startDateLocal.formatDate().padEnd(DATE_PAD)
                        + activity.name.padEnd(maxActivityNameLength + 1) +
                        "%.02f km".format(activity.distance / 1000).padEnd(DISTANCE_PAD)
                        + activity.elapsedTime.formatSeconds().padEnd(TIME_PAD)
                        + activity.speed()
            )
        }
    }

    /**
     * LoadActivities
     */
    private fun loadActivities(): List<Activity> {

        val activities = when {
            // from file
            parameters.file != null -> activityLoader.getActivitiesFromFile(
                parameters.file!!
            )
            // with access token
            parameters.accessToken != null -> activityLoader.getActivitiesWithAccessToken(
                parameters.clientId,
                parameters.year,
                parameters.accessToken!!
            )
            // with access authorization code
            parameters.code != null -> activityLoader.getActivitiesWithAuthorizationCode(
                parameters.clientId,
                parameters.year,
                parameters.clientSecret,
                parameters.code!!
            )
            else -> throw ParameterException("-file, -code or -accessToken must be provided")
        }

        println("Nb activities loaded : ${activities.size}")

        return activities
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

fun main(incomingArgs: Array<String>) {
    MyStravaStats(incomingArgs).run()
}





