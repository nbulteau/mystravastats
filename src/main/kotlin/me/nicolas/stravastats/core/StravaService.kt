package me.nicolas.stravastats.core


import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.nicolas.stravastats.core.business.Stats
import me.nicolas.stravastats.core.business.StravaStats
import me.nicolas.stravastats.infrastructure.StravaApi
import me.nicolas.stravastats.infrastructure.dao.Activity

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths
import java.time.Duration
import java.time.LocalDate.now
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.exp


internal class StravaService(
    private val stravaApi: StravaApi
) {

    private val logger: Logger = LoggerFactory.getLogger(StravaService::class.java)

    private val objectMapper = jacksonObjectMapper()

    fun computeStatistics(activities: List<Activity>): StravaStats {

        val commuteRideStats = extractStats(activities.filter { it.type == "Ride" && it.commute })
        val sportRideStats = extractStats(activities.filter { it.type == "Ride" && !it.commute })
        val runsStats = extractStats(activities.filter { it.type == "Run" })
        val hikesStats = extractStats(activities.filter { it.type == "Hike" })

        return StravaStats(commuteRideStats, sportRideStats, runsStats, hikesStats)
    }

    fun getActivitiesFromFile(filePath: String): List<Activity> {

        logger.info("Get activities from file : $filePath")

        val objectMapper = ObjectMapper()
        val activities: Array<Activity> =
            objectMapper.readValue(
                Paths.get(filePath).toFile(),
                Array<Activity>::class.java
            )

        return activities.toList()
    }

    fun getActivitiesWithAuthorizationCode(clientId: String, year: Int, clientSecret: String, authorizationCode: String): List<Activity> {

        logger.info("Get activities with code : $authorizationCode")

        val token = stravaApi.getToken(clientId, clientSecret, authorizationCode)

        return getActivitiesWithAccessToken(clientId, year, token.accessToken)
    }

    fun getActivitiesWithAccessToken(clientId: String, year: Int, accessToken: String): List<Activity> {

        logger.info("Get activities with accessToken : $accessToken")

        val activities = stravaApi.getActivities(
            accessToken = accessToken,
            before = LocalDateTime.of(year, 12, 31, 23, 59),
            after = LocalDateTime.of(year, 1, 1, 0, 0)
        )

        saveToFile(clientId, year, activities)

        return activities
    }

    /**
     * Save activities to file.
     * @param clientId
     * @param year
     * @param activities
     */
    private fun saveToFile(
        clientId: String,
        year: Int,
        activities: List<Activity>
    ) {
        val writer: ObjectWriter = objectMapper.writer(DefaultPrettyPrinter())
        writer.writeValue(File("strava-$clientId-$year-${now()}.json"), activities)
    }

    /**
     * Extract statistics.
     * @param activities
     */
    private fun extractStats(activities: List<Activity>): Stats {

        val totalDistance = activities.sumByDouble { it.distance } / 1000
        val totalElevationGain = activities.sumByDouble { it.totalElevationGain }
        return Stats(activities.size, totalDistance, totalElevationGain)
    }

    fun calculateVo2max(distance: Double, duration: Duration): Double {

        //calculate velocity im metres per min
        val velocity = distance * 1000 / duration.seconds

        //calculate % max
        val percentMax = 0.8 + (0.1894393 * exp(-0.012778 * duration.seconds)) + (0.2989558 * exp(-0.1932605 * duration.seconds))

        //calculate vo2
        val vo2 = -4.60 + (0.182258 * velocity) + (0.000104 * velocity * velocity)

        //calculate vo2 max
        return vo2 / percentMax
    }
}