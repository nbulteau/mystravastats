package me.nicolas.stravastats.service.csv

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Hike
import me.nicolas.stravastats.service.statistics.calculateBestDistanceForTime
import me.nicolas.stravastats.service.statistics.calculateBestElevationForDistance
import me.nicolas.stravastats.service.statistics.calculateBestTimeForDistance
import me.nicolas.stravastats.utils.formatDate
import me.nicolas.stravastats.utils.formatSeconds

internal class HikeCSVExporter(clientId: String, activities: List<Activity>, year: String) :
    CSVExporter(clientId, activities, year, Hike) {

    override fun generateHeader() {
        writeCSVLine(
            listOf(
                "Date",
                "Description",
                "Distance (km)",
                "Time",
                "Time (seconds)",
                "Speed (km/h)",
                "Elevation (m)",
                "Highest point (m)",
                "Best 1000m (km/h)",
                "Best 1 h (km/h)",
                "Max gradient for 250 m (%)",
                "Max gradient for 500 m (%)",
                "Max gradient for 1000 m (%)",
                "Max gradient for 5 km (%)",
                "Max gradient for 10 km (%)",
            )
        )
    }

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
                    "%.0f".format(activity.totalElevationGain),
                    "%.0f".format(activity.elevHigh),
                    activity.calculateBestTimeForDistance(1000.0)?.getSpeed() ?: "",
                    activity.calculateBestDistanceForTime(60 * 60)?.getSpeed() ?: "",
                    activity.calculateBestElevationForDistance(250.0)?.getGradient() ?: "",
                    activity.calculateBestElevationForDistance(500.0)?.getGradient() ?: "",
                    activity.calculateBestElevationForDistance(1000.0)?.getGradient() ?: "",
                    activity.calculateBestElevationForDistance(5000.0)?.getGradient() ?: "",
                    activity.calculateBestElevationForDistance(10000.0)?.getGradient() ?: "",
                )
            )
        }
    }

    override fun generateFooter() {
        val lastRow = activities.size + 1
        writeCSVLine(
            listOf(
                ";;" +
                        "=SOMME(\$C2:\$C$lastRow);;" +
                        "=SOMME(\$E2:\$E$lastRow);" +
                        "=MAX(\$F2:\$F$lastRow);" +
                        "=MAX(\$G2:\$G$lastRow);" +
                        "=MAX(\$H2:\$H$lastRow);" +
                        "=MAX(\$I2:\$I$lastRow);" +
                        "=MAX(\$J2:\$J$lastRow);" +
                        "=MAX(\$K2:\$K$lastRow);" +
                        "=MAX(\$L2:\$L$lastRow);" +
                        "=MAX(\$M2:\$M$lastRow)"
            )
        )
    }
}