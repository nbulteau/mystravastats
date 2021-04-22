package me.nicolas.stravastats.core

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.core.statistics.calculateBestDistanceForTime
import me.nicolas.stravastats.core.statistics.calculateBestElevationForDistance
import me.nicolas.stravastats.core.statistics.calculateBestTimeForDistance
import me.nicolas.stravastats.helpers.formatDate
import me.nicolas.stravastats.helpers.formatSeconds
import me.nicolas.stravastats.helpers.writeCSVLine
import java.io.File
import java.io.FileWriter

internal class CSVExporter {

    fun exportCSV(clientId: String, activities: List<Activity>, filter: Double?) {
        activities
            .groupBy { activity ->
                activity.startDateLocal.subSequence(0, 4).toString()
            } // year by year
            .forEach { map: Map.Entry<String, List<Activity>> ->
                if (filter != null) {
                    exportCSV(clientId, filterActivities(map.value, filter), map.key.toInt())
                }
            }
    }

    private fun exportCSV(clientId: String, activities: List<Activity>, year: Int) {
        print("* Export activities for $year [")
        print("Ride")
        exportBikeCSV(
            clientId = clientId,
            activities = activities.filter { activity -> activity.type == "Ride" },
            type = "Ride",
            year = year
        )
        print(", Run")
        exportRunCSV(
            clientId = clientId,
            activities = activities.filter { activity -> activity.type == "Run" },
            type = "Run",
            year = year
        )
        print(", Hike")
        exportHikeCSV(
            clientId = clientId,
            activities = activities.filter { activity -> activity.type == "Hike" },
            type = "Hike",
            year = year
        )
        print(", InlineSkate")
        exportInLineSkateCSV(
            clientId = clientId,
            activities = activities.filter { activity -> activity.type == "InlineSkate" },
            type = "InlineSkate",
            year = year
        )
        println("]")

    }

    private fun filterActivities(activities: List<Activity>, filter: Double): List<Activity> {
        val lowBoundary = filter - (5 * filter / 100)
        val highBoundary = filter + (5 * filter / 100)
        return activities.filter { activity -> activity.distance > lowBoundary && activity.distance < highBoundary }
    }

    private fun exportBikeCSV(clientId: String, activities: List<Activity>, type: String, year: Int) {

        // if no activities : nothing to do
        if (activities.isEmpty()) {
            return
        }

        val writer = FileWriter(File("$clientId-$type-$year.csv"))
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

    private fun exportRunCSV(clientId: String, activities: List<Activity>, type: String, year: Int) {

        // if no activities : nothing to do
        if (activities.isEmpty()) {
            return
        }

        val writer = FileWriter(File("$clientId-$type-$year.csv"))
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

    private fun exportInLineSkateCSV(clientId: String, activities: List<Activity>, type: String, year: Int) {

        // if no activities : nothing to do
        if (activities.isEmpty()) {
            return
        }

        val writer = FileWriter(File("$clientId-$type-$year.csv"))
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

    private fun exportHikeCSV(clientId: String, activities: List<Activity>, type: String, year: Int) {

        // if no activities : nothing to do
        if (activities.isEmpty()) {
            return
        }

        val writer = FileWriter(File("$clientId-$type-$year.csv"))
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