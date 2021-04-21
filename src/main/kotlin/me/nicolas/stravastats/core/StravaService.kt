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

        val commuteRideStats =
            statsBuilder.computeCommuteBikeStats(filteredActivities.filter { it.type == "Ride" && it.commute })
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
                "Time (seconds)",
                "Speed (km/h)",
                "Best 250m (km/h)",
                "Best 500m (km/h)",
                "Best 1000m (km/h)",
                "Best 5km (km/h)",
                "Best 10km (km/h)",
                "Best 20km (km/h)",
                "Best 50km (km/h)",
                "Best 100km (km/h)",
                "Best 30 min (km/h)",
                "Best 1 h (km/h)",
                "Best 2 h (km/h)",
                "Best 3 h (km/h)",
                "Best 4 h (km/h)",
                "Best 5 h (km/h)",
                "Max gradient for 250 m (%)",
                "Max gradient for 500 m (%)",
                "Max gradient for 1000 m (%)",
                "Max gradient for 5 km (%)",
                "Max gradient for 10 km (%)",
                "Max gradient for 20 km (%)",
            ).writeCSVLine(writer)

            activities.forEach { activity ->

                listOf(
                    activity.startDateLocal.formatDate(),
                    activity.name.trim(),
                    "%.02f".format(activity.distance / 1000),
                    activity.elapsedTime.formatSeconds(),
                    "%d".format(activity.elapsedTime),
                    activity.getSpeed(),
                    activity.calculateBestTimeForDistance(250.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(500.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(1000.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(5000.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(10000.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(20000.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(50000.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(100000.0)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(30 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(60 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(2 * 60 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(3 * 60 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(4 * 60 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(5 * 60 * 60)?.getSpeed() ?: "",
                    activity.calculateBestElevationForDistance(250.0)?.getSlope() ?: "",
                    activity.calculateBestElevationForDistance(500.0)?.getSlope() ?: "",
                    activity.calculateBestElevationForDistance(1000.0)?.getSlope() ?: "",
                    activity.calculateBestElevationForDistance(5000.0)?.getSlope() ?: "",
                    activity.calculateBestElevationForDistance(10000.0)?.getSlope() ?: "",
                    activity.calculateBestElevationForDistance(20000.0)?.getSlope() ?: "",
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
                "Time (seconds)",
                "Speed (min/km)",
                "Best 200m (min/km)",
                "Best 400m (min/km)",
                "Best 1000m (min/km)",
                "Best 10000m (min/km)",
                "Best half Marathon (min/km)",
                "Best Marathon (min/km)",
                "Best 30 min (min/km)",
                "Best 1 h (min/km)",
                "Best 2 h (min/km)",
                "Best 3 h (min/km)",
                "Best 4 h (min/km)",
                "Best 5 h (min/km)",
                "Best vVO2max = 6 min (min/km)",
            ).writeCSVLine(writer)

            activities.forEach { activity ->

                listOf(
                    activity.startDateLocal.formatDate(),
                    activity.name.trim(),
                    "%.02f".format(activity.distance / 1000),
                    activity.elapsedTime.formatSeconds(),
                    "%d".format(activity.elapsedTime),
                    activity.getSpeed(),
                    activity.calculateBestTimeForDistance(200.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(400.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(1000.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(10000.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(21097.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(42195.0)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(30 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(60 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(2 * 60 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(3 * 60 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(4 * 60 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(5 * 60 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(12 * 60)?.getSpeed() ?: ""
                ).writeCSVLine(writer)
            }
        }
    }

    /**
     * Export to CSV file.
     * @param activities activities to export.
     */
    fun exportInLineSkateCSV(activities: List<Activity>, type: String, year: Int) {

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
                "Time (seconds)",
                "Speed (km/h)",
                "Best 200m (km/h)",
                "Best 400m (km/h)",
                "Best 1000m (km/h)",
                "Best 10000m (km/h)",
                "Best half Marathon (km/h)",
                "Best Marathon (km/h)",
                "Best 30 min (km/h)",
                "Best 1 h (km/h)",
                "Best 2 h (km/h)",
                "Best 3 h (km/h)",
                "Best 4 h (km/h)",
                "Best 5 h (km/h)",
                "Best vVO2max = 6 min (min/k)",
            ).writeCSVLine(writer)

            activities.forEach { activity ->

                listOf(
                    activity.startDateLocal.formatDate(),
                    activity.name.trim(),
                    "%.02f".format(activity.distance / 1000),
                    activity.elapsedTime.formatSeconds(),
                    "%d".format(activity.elapsedTime),
                    activity.getSpeed(),
                    activity.calculateBestTimeForDistance(200.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(400.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(1000.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(10000.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(21097.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(42195.0)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(30 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(60 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(2 * 60 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(3 * 60 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(4 * 60 * 60)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(5 * 60 * 60)?.getSpeed() ?: "",
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
                "Time (seconds)",
                "Speed (km/h)",
                "Elevation (m)",
                "Highest point (m)",
                "Best 200m (km/h)",
                "Best 400m (km/h)",
                "Best 1000m (km/h)",
                "Best 10000m (km/h)",
                "Best 1 h (km/h)",
            ).writeCSVLine(writer)

            activities.forEach { activity ->

                listOf(
                    activity.startDateLocal.formatDate(),
                    activity.name.trim(),
                    "%.02f".format(activity.distance / 1000),
                    activity.elapsedTime.formatSeconds(),
                    "%d".format(activity.elapsedTime),
                    activity.getSpeed(),
                    "%.0f".format(activity.totalElevationGain),
                    "%.0f".format(activity.elevHigh),
                    activity.calculateBestTimeForDistance(200.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(400.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(1000.0)?.getSpeed() ?: "",
                    activity.calculateBestTimeForDistance(10000.0)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(60 * 60)?.getSpeed() ?: "",
                ).writeCSVLine(writer)
            }
        }
    }
}