package me.nicolas.stravastats

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import me.nicolas.stravastats.core.StravaService
import me.nicolas.stravastats.core.business.StravaStats
import me.nicolas.stravastats.infrastructure.StravaApi

class StravaStats(incomingArgs: Array<String>) {

    private val stravaService = StravaService(StravaApi())

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
            val statistics = when {
                // from file
                parameters.file != null -> stravaService.getActivitiesFromFile(
                    parameters.file!!
                )
                // with access token
                parameters.accessToken != null -> stravaService.getActivitiesWithAccessToken(
                    parameters.clientId,
                    parameters.year,
                    parameters.accessToken!!
                )
                // with access authorization code
                parameters.code != null -> stravaService.getActivitiesWithAuthorizationCode(
                    parameters.clientId,
                    parameters.year,
                    parameters.clientSecret,
                    parameters.code!!
                )
                else -> throw ParameterException("-file, -code or -accessToken must be provided")
            }

            return stravaService.computeStatistics(statistics)
        }
}

fun main(incomingArgs: Array<String>) {

    val stavaStats = StravaStats(incomingArgs).stavaStats

    stavaStats.displayStatistics()
}





