package me.nicolas.stravastats.service.csv

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Run
import me.nicolas.stravastats.utils.formatDate
import me.nicolas.stravastats.utils.formatSeconds
import me.nicolas.stravastats.service.statistics.calculateBestDistanceForTime
import me.nicolas.stravastats.service.statistics.calculateBestTimeForDistance

internal class RunCSVExporter(clientId: String, activities: List<Activity>, year: String) :
    CSVExporter(clientId, activities, year, Run) {

    override fun generateActivities() {
        activities.forEach { activity ->
            writeCSVLine(
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
                )
            )
        }
    }

    override fun generateHeader() {
        writeCSVLine(
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
            )
        )
    }

    override fun generateFooter() {
        val lastRow = activities.size + 1

        writeCSVLine(
            listOf(
                ";;" +
                        "=SOMME(\$C2:\$C$lastRow);;" +
                        "=SOMME(\$E2:\$E$lastRow);"
            )
        )
    }
}