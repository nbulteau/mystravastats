package me.nicolas.stravastats

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import me.nicolas.stravastats.core.ActivityLoader
import me.nicolas.stravastats.core.StatsBuilder
import me.nicolas.stravastats.core.StravaService
import me.nicolas.stravastats.core.business.formatDate
import me.nicolas.stravastats.core.business.formatSeconds
import me.nicolas.stravastats.infrastructure.StravaApi
import me.nicolas.stravastats.infrastructure.dao.Activity

internal class MyStravaStats(incomingArgs: Array<String>) {

    private val stravaStatsProperties = StravaStatsProperties()

    private val stravaApi = StravaApi()

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

        val activities = loadActivities(parameters)

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

    private fun displayStatistics(activities: List<Activity>) {
        val stravaStats = stravaService.computeStatistics(activities)
        stravaStats.displayStatistics()
    }

    private fun displayActivities(activities: List<Activity>) {
        println("* Activities")
        // apply filter if exist
        val filteredActivities = if (parameters.filter != null) {
            val lowBoundary = parameters.filter!! - (5 * parameters.filter!! / 100)
            val highBoundary = parameters.filter!! + (5 * parameters.filter!! / 100)

            activities.filter { activity -> activity.distance > lowBoundary && activity.distance < highBoundary }
        } else {
            activities
        }
        println("** Rides")
        doDisplayActivities(filteredActivities.filter { it.type == "Ride" })
        println("** Run")
        doDisplayActivities(filteredActivities.filter { it.type == "Run" })
        println("** Hike")
        doDisplayActivities(filteredActivities.filter { it.type == "Hike" })
    }

    private fun doDisplayActivities(activities: List<Activity>) {

        // if no activities : nothing to do
        if (activities.isEmpty()) {
            return
        }

        // list all activities
        val maxActivityNameLength = activities.maxByOrNull { it.name.length }?.name?.length!!

        println(
            "Date".padEnd(32)
                    + "Description".padEnd(maxActivityNameLength + 1)
                    + "Distance".padEnd(10)
                    + "Time".padEnd(14)
                    + "Speed"
        )
        activities.forEach { activity ->
            println(
                activity.startDateLocal.formatDate().padEnd(32)
                        + activity.name.padEnd(maxActivityNameLength + 1) +
                        "%.02f km".format(activity.distance / 1000).padEnd(10)
                        + activity.elapsedTime.formatSeconds().padEnd(14)
                        +
                        if (activity.type == "Run") {
                            "%s/km".format((activity.elapsedTime * 1000 / activity.distance).formatSeconds())
                        } else {
                            "%.02f km/h".format(activity.distance / activity.elapsedTime * 3600 / 1000)
                        }
            )
        }
    }

    private fun loadActivities(parameters: Parameters): List<Activity> = when {
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
}

fun main(incomingArgs: Array<String>) {
    MyStravaStats(incomingArgs).run()
}





