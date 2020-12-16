package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.helpers.dateFormatter
import me.nicolas.stravastats.helpers.inDateTimeFormatter
import java.time.LocalDate
import java.time.LocalDateTime

internal class MaxElevationInADayStatistic(
    activities: List<Activity>
) : Statistic("Max elevation gain in a day", activities) {

    private val mostActiveDay: Map.Entry<String, Double>? =
        activities.groupBy { activity -> activity.startDateLocal.substringBefore('T') }
            .mapValues { (_, activities) -> activities.sumByDouble { activity -> activity.totalElevationGain } }
            .maxByOrNull { it.value }

    private fun getDay(startDateLocal: String) = LocalDateTime.parse(startDateLocal, inDateTimeFormatter).dayOfYear

    override fun toString(): String {
        return super.toString() + if (mostActiveDay != null) {
            "%.2f m - %s".format(mostActiveDay.value, LocalDate.parse(mostActiveDay.key).format(dateFormatter))
        } else {
            "Not available"
        }
    }
}