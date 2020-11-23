package me.nicolas.stravastats

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import me.nicolas.stravastats.core.ActivityLoader
import me.nicolas.stravastats.core.StatsBuilder
import me.nicolas.stravastats.core.StravaService
import me.nicolas.stravastats.core.business.statistics.StravaStats
import me.nicolas.stravastats.infrastructure.StravaApi
import me.nicolas.stravastats.infrastructure.dao.Activity

internal class StravaStats(incomingArgs: Array<String>) {

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

    val stavaStats: StravaStats
        get() {
            val activities = loadActivities(parameters)

            if (stravaStatsProperties.removingNonMovingSections) {
                activities.forEach { it.removeNonMoving() }
            }

            return stravaService.computeStatistics(activities)
        }

    private fun loadActivities(parameters: Parameters): List<Activity> {

        return when {
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
}

fun main(incomingArgs: Array<String>) {

    val stavaStats = StravaStats(incomingArgs).stavaStats

    stavaStats.displayStatistics()
}





