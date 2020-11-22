package me.nicolas.stravastats.core


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.nicolas.stravastats.core.business.Stats
import me.nicolas.stravastats.core.business.StravaStats
import me.nicolas.stravastats.infrastructure.StravaApi
import me.nicolas.stravastats.infrastructure.dao.Activity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
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