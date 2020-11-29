package me.nicolas.stravastats.core


import me.nicolas.stravastats.core.business.statistics.StravaStats
import me.nicolas.stravastats.infrastructure.dao.Activity


internal class StravaService(
    private val statsBuilder: StatsBuilder
) {

    fun computeStatistics(activities: List<Activity>): StravaStats {

        val commuteRideStats = statsBuilder.computeStats(activities.filter { it.type == "Ride" && it.commute })
        val sportRideStats = statsBuilder.computeBikeStats(activities.filter { it.type == "Ride" && !it.commute })
        val runsStats = statsBuilder.computeRunStats(activities.filter { it.type == "Run" })
        val hikesStats = statsBuilder.computeHikeStats(activities.filter { it.type == "Hike" })

        return StravaStats(commuteRideStats, sportRideStats, runsStats, hikesStats)
    }

}