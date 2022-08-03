package me.nicolas.stravastats.service.csv

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.InlineSkate
import me.nicolas.stravastats.utils.formatDate
import me.nicolas.stravastats.utils.formatSeconds
import me.nicolas.stravastats.service.statistics.calculateBestDistanceForTime
import me.nicolas.stravastats.service.statistics.calculateBestTimeForDistance

internal class InlineSkateCSVExporter(clientId: String, activities: List<Activity>, year: String) :
    CSVExporter(clientId, activities, year, InlineSkate) {

    override fun generateHeader() {
        writeCSVLine(
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
                        "=MAX(\$M2:\$M$lastRow);" +
                        "=MAX(\$N2:\$N$lastRow);" +
                        "=MAX(\$O2:\$O$lastRow);" +
                        "=MAX(\$P2:\$P$lastRow);" +
                        "=MAX(\$Q2:\$Q$lastRow);" +
                        "=MAX(\$R2:\$R$lastRow)"
            )
        )
    }
}