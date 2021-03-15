package me.nicolas.stravastats.core


import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.core.statistics.StravaStatistics
import me.nicolas.stravastats.core.statistics.calculateBestDistanceForTime
import me.nicolas.stravastats.core.statistics.calculateBestElevationForDistance
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
        val inlineSkate = statsBuilder.computeInlineSkateStats(filteredActivities.filter { it.type == "InlineSkate" })
        return StravaStatistics(globalStatistics, commuteRideStats, sportRideStats, runsStats, hikesStats, inlineSkate)
    }

    /**
     * Export to CSV file.
     * @param activities activities to export.
     */
    fun exportBikeCSV(activities: List<Activity>, type: String, year: Int) {

        // if no activities : nothing to do
        if (activities.isEmpty()) {
            return
        }

        val writer = FileWriter(File("activities-$type-$year.csv"))
        writer.use {
            listOf(
                "Date",
                "Description",
                "Distance (km)",
                "Time",
                "Speed (km/h)",
                "Best 250m",
                "Best 500m",
                "Best 1000m",
                "Best 5km",
                "Best 10km",
                "Best 20km",
                "Best 50km",
                "Best 100km",
                "Best 30 min",
                "Best 1 h",
                "Best 2 h",
                "Best 3 h",
                "Best 4 h",
                "Best 5 h",
                "Max gradient for 250 m",
                "Max gradient for 500 m",
                "Max gradient for 1000 m",
                "Max gradient for 5 km",
                "Max gradient for 10 km",
                "Max gradient for 20 km",
            ).writeCSVLine(writer)

            activities.forEach { activity ->

                listOf(
                    activity.startDateLocal.formatDate(),
                    activity.name.trim(),
                    "%.02f".format(activity.distance / 1000),
                    activity.elapsedTime.formatSeconds(),
                    activity.getFormattedSpeed(),
                    activity.calculateBestTimeForDistance(250.0)?.getFormattedSpeed() ?: "",
                    activity.calculateBestTimeForDistance(500.0)?.getFormattedSpeed() ?: "",
                    activity.calculateBestTimeForDistance(1000.0)?.getFormattedSpeed() ?: "",
                    activity.calculateBestTimeForDistance(5000.0)?.getFormattedSpeed() ?: "",
                    activity.calculateBestTimeForDistance(10000.0)?.getFormattedSpeed() ?: "",
                    activity.calculateBestTimeForDistance(20000.0)?.getFormattedSpeed() ?: "",
                    activity.calculateBestTimeForDistance(50000.0)?.getFormattedSpeed() ?: "",
                    activity.calculateBestTimeForDistance(100000.0)?.getFormattedSpeed() ?: "",
                    activity.calculateBestDistanceForTime(30 * 60)?.getFormattedSpeed() ?: "",
                    activity.calculateBestDistanceForTime(60 * 60)?.getFormattedSpeed() ?: "",
                    activity.calculateBestDistanceForTime(2 * 60 * 60)?.getFormattedSpeed() ?: "",
                    activity.calculateBestDistanceForTime(3 * 60 * 60)?.getFormattedSpeed() ?: "",
                    activity.calculateBestDistanceForTime(4 * 60 * 60)?.getFormattedSpeed() ?: "",
                    activity.calculateBestDistanceForTime(5 * 60 * 60)?.getFormattedSpeed() ?: "",
                    activity.calculateBestElevationForDistance(250.0)?.getFormattedSlope() ?: "",
                    activity.calculateBestElevationForDistance(500.0)?.getFormattedSlope() ?: "",
                    activity.calculateBestElevationForDistance(1000.0)?.getFormattedSlope() ?: "",
                    activity.calculateBestElevationForDistance(5000.0)?.getFormattedSlope() ?: "",
                    activity.calculateBestElevationForDistance(10000.0)?.getFormattedSlope() ?: "",
                    activity.calculateBestElevationForDistance(20000.0)?.getFormattedSlope() ?: "",
                ).writeCSVLine(writer)
            }
        }
    }

    /**
     * Export to CSV file.
     * @param activities activities to export.
     */
    fun exportRunCSV(activities: List<Activity>, type: String, year: Int) {

        // if no activities : nothing to do
        if (activities.isEmpty()) {
            return
        }

        val writer = FileWriter(File("activities-$type-$year.csv"))
        writer.use {
            listOf(
                "Date",
                "Description",
                "Distance (km)",
                "Time",
                "Speed (min/k)",
                "Best 200m",
                "Best 400m",
                "Best 1000m",
                "Best 10000m",
                "Best half Marathon",
                "Best Marathon",
                "Best 30 min",
                "Best 1 h",
                "Best 2 h",
                "Best 3 h",
                "Best 4 h",
                "Best 5 h",
                "Best vVO2max = 6 min (min/k)",
            ).writeCSVLine(writer)

            activities.forEach { activity ->

                listOf(
                    activity.startDateLocal.formatDate(),
                    activity.name.trim(),
                    "%.02f".format(activity.distance / 1000),
                    activity.elapsedTime.formatSeconds(),
                    activity.getFormattedSpeed(),
                    activity.calculateBestTimeForDistance(200.0)?.getFormattedSpeed() ?: "",
                    activity.calculateBestTimeForDistance(400.0)?.getFormattedSpeed() ?: "",
                    activity.calculateBestTimeForDistance(1000.0)?.getFormattedSpeed() ?: "",
                    activity.calculateBestTimeForDistance(10000.0)?.getFormattedSpeed() ?: "",
                    activity.calculateBestTimeForDistance(21097.0)?.getFormattedSpeed() ?: "",
                    activity.calculateBestTimeForDistance(42195.0)?.getFormattedSpeed() ?: "",
                    activity.calculateBestDistanceForTime(30 * 60)?.getFormattedSpeed() ?: "",
                    activity.calculateBestDistanceForTime(60 * 60)?.getFormattedSpeed() ?: "",
                    activity.calculateBestDistanceForTime(2 * 60 * 60)?.getFormattedSpeed() ?: "",
                    activity.calculateBestDistanceForTime(3 * 60 * 60)?.getFormattedSpeed() ?: "",
                    activity.calculateBestDistanceForTime(4 * 60 * 60)?.getFormattedSpeed() ?: "",
                    activity.calculateBestDistanceForTime(5 * 60 * 60)?.getFormattedSpeed() ?: "",
                    activity.calculateBestDistanceForTime(12 * 60)
                        ?.run { "%s/km".format((seconds * 1000 / distance).formatSeconds()) } ?: ""
                ).writeCSVLine(writer)
            }
        }
    }

    /**
     * Export to CSV file.
     * @param activities activities to export.
     */
    fun exportHikeCSV(activities: List<Activity>, type: String, year: Int) {

        // if no activities : nothing to do
        if (activities.isEmpty()) {
            return
        }

        val writer = FileWriter(File("activities-$type-$year.csv"))
        writer.use {
            listOf(
                "Date",
                "Description",
                "Distance (km)",
                "Time",
                "Elevation (m)",
                "Highest point (m)",
                "Best 1000m",
                "Best 1 h",
            ).writeCSVLine(writer)

            activities.forEach { activity ->

                listOf(
                    activity.startDateLocal.formatDate(),
                    activity.name.trim(),
                    "%.02f".format(activity.distance / 1000),
                    activity.elapsedTime.formatSeconds(),
                    "%.0f".format(activity.totalElevationGain),
                    "%.0f".format(activity.elevHigh),
                    activity.calculateBestTimeForDistance(1000.0)?.getFormattedSpeed() ?: "",
                    activity.calculateBestDistanceForTime(60 * 60)?.getFormattedSpeed() ?: "",
                ).writeCSVLine(writer)
            }
        }
    }
}