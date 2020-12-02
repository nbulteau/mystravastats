package me.nicolas.stravastats.core


import me.nicolas.stravastats.core.business.statistics.StravaStats
import me.nicolas.stravastats.infrastructure.dao.Activity


internal class StravaService(
    private val statsBuilder: StatsBuilder
) {

    fun computeStatistics(activities: List<Activity>): StravaStats {

        val globalStatistics = statsBuilder.computeGlobalStats(activities)

        // filter activities without streams
        val filteredActivities = activities
            .filter { it.stream != null && it.stream?.time != null && it.stream?.distance != null && it.stream?.altitude != null }
        println("Nb activities used to compute statistics (with streams) : ${filteredActivities.size}")

        val commuteRideStats = statsBuilder.computeStats(filteredActivities.filter { it.type == "Ride" && it.commute })
        val sportRideStats =
            statsBuilder.computeBikeStats(filteredActivities.filter { it.type == "Ride" && !it.commute })
        val runsStats = statsBuilder.computeRunStats(filteredActivities.filter { it.type == "Run" })
        val hikesStats = statsBuilder.computeHikeStats(filteredActivities.filter { it.type == "Hike" })

        return StravaStats(globalStatistics, commuteRideStats, sportRideStats, runsStats, hikesStats)
    }

}