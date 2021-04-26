package me.nicolas.stravastats.core.csv

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Run
import me.nicolas.stravastats.core.formatDate
import me.nicolas.stravastats.core.formatSeconds
import me.nicolas.stravastats.core.statistics.calculateBestDistanceForTime
import me.nicolas.stravastats.core.statistics.calculateBestTimeForDistance
import java.io.FileWriter

internal class RunCSVExporter(activities: List<Activity>) : CSVExporter(activities, Run) {

    override fun generateActivities(writer: FileWriter) {
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

    override fun generateHeader(writer: FileWriter) {
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
    }

    override fun generateFooter(writer: FileWriter) {
        val lastRow = activities.size + 1

        listOf(
            ";;" +
                    "=SOMME(\$C2:\$C$lastRow);;" +
                    "=SOMME(\$E2:\$E$lastRow);"
        ).writeCSVLine(writer)
    }
}