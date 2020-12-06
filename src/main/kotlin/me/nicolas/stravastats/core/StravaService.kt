package me.nicolas.stravastats.core


import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.core.statistics.StravaStatistics


internal class StravaService(
    private val statsBuilder: StatsBuilder
) {

    fun computeStatistics(activities: List<Activity>): StravaStatistics {

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

        return StravaStatistics(globalStatistics, commuteRideStats, sportRideStats, runsStats, hikesStats)
    }

}