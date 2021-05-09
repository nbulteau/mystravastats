package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Statistic
import java.time.Month
import java.time.format.TextStyle
import java.util.*

internal class MostActiveMonthStatistic(
    activities: List<Activity>
) : Statistic("Most active month", activities) {

    private val mostActiveMonth =
        activities.groupBy { activity -> activity.startDateLocal.substringBeforeLast('-') }
            .mapValues { (_, activities) -> activities.sumOf { activity -> activity.distance } }
            .maxByOrNull { entry: Map.Entry<String, Double> -> entry.value }

    override fun display() = if (mostActiveMonth != null) {
        "%s with %.2f km".format(formatMonth(mostActiveMonth.key), mostActiveMonth.value.div(1000))
    } else {
        "Not available"
    }

    private fun formatMonth(input: String): String {
        val parts = input.split("-")
        return "${Month.of(parts[1].toInt()).getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${parts[0]}"
    }
}

