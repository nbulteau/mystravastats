package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Statistic
import me.nicolas.stravastats.core.dateFormatter
import java.time.LocalDate

internal class MaxElevationInADayStatistic(
    activities: List<Activity>
) : Statistic(name = "Max elevation gain in a day", activities) {

    private val mostActiveDay: Map.Entry<String, Double>? =
        activities.groupBy { activity -> activity.startDateLocal.substringBefore('T') }
            .mapValues { (_, activities) -> activities.sumOf { activity -> activity.totalElevationGain } }
            .maxByOrNull { it.value }

    override fun display() = if (mostActiveDay != null) {
        "%.2f m - %s".format(mostActiveDay.value, LocalDate.parse(mostActiveDay.key).format(dateFormatter))
    } else {
        "Not available"
    }
}