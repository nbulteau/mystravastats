package me.nicolas.stravastats.core


import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.core.statistics.StravaStatistics
import me.nicolas.stravastats.core.statistics.calculateBestTimeForDistance
import me.nicolas.stravastats.helpers.formatDate
import me.nicolas.stravastats.helpers.formatSeconds
import me.nicolas.stravastats.helpers.writeCSVLine
import java.io.File
import java.io.FileWriter


internal class StravaService(
    private val statsBuilder: StatsBuilder
) {

    /**
     * Compute statistics.
     * @param activities activities to scan.
     */
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

    /**
     * Export to CSV file.
     * @param activities activities to export.
     */
    fun exportCSV(activities: List<Activity>, type: String, year: Int) {

        // if no activities : nothing to do
        if (activities.isEmpty()) {
            return
        }

        val writer = FileWriter(File("activities-$type-$year.csv"))
        writer.use {
            listOf(
                "Date", "Description", "Distance (km)", "Time", "Speed", "Best 1000m"
            ).writeCSVLine(writer)

            activities.forEach { activity ->

                listOf(
                    activity.startDateLocal.formatDate(),
                    activity.name.trim(),
                    "%.02f".format(activity.distance / 1000),
                    activity.elapsedTime.formatSeconds(),
                    activity.getFormattedSpeed(),
                    activity.calculateBestTimeForDistance(1000.0)?.getFormattedSpeed() ?: "",
                ).writeCSVLine(writer)
            }
        }
    }
}