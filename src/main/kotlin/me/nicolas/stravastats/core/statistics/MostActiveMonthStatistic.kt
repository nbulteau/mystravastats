package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import java.time.Month
import java.time.format.TextStyle
import java.util.*

internal class MostActiveMonthStatistic(
    activities: List<Activity>
) : Statistic("Most active month", activities) {

    private val mostActiveMonth =
        activities.groupBy { activity -> activity.startDateLocal.substringBeforeLast('-') }
            .mapValues { (_, activities) -> activities.sumByDouble { activity -> activity.distance } }
            .maxByOrNull { it.value }

    override fun toString(): String {
        return super.toString() + if (mostActiveMonth != null) {
            "%s with %.2f km".format(formatMonth(mostActiveMonth.key), mostActiveMonth.value.div(1000))
        } else {
            "Not available"
        }
    }

    private fun formatMonth(input: String): String {
        val parts = input.split("-")
        return "${Month.of(parts[1].toInt()).getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${parts[0]}"
    }
}

