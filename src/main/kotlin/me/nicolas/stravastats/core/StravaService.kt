package me.nicolas.stravastats.core


import me.nicolas.stravastats.core.business.statistics.StravaStats
import me.nicolas.stravastats.infrastructure.dao.Activity
import org.slf4j.Logger
import org.slf4j.LoggerFactory


internal class StravaService(
    private val statsBuilder: StatsBuilder
) {

    private val logger: Logger = LoggerFactory.getLogger(StravaService::class.java)

    fun computeStatistics(activities: List<Activity>): StravaStats {

        logger.info("Compute bike statistics.")
        val commuteRideStats = statsBuilder.computeStats(activities.filter { it.type == "Ride" && it.commute })
        val sportRideStats = statsBuilder.computeBikeStats(activities.filter { it.type == "Ride" && !it.commute })
        logger.info("Compute run statistics.")
        val runsStats = statsBuilder.computeRunStats(activities.filter { it.type == "Run" })
        logger.info("Compute hike statistics.")
        val hikesStats = statsBuilder.computeStats(activities.filter { it.type == "Hike" })

        return StravaStats(commuteRideStats, sportRideStats, runsStats, hikesStats)
    }

}