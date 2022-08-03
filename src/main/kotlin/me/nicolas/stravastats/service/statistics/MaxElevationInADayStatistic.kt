package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.utils.dateFormatter
import java.time.LocalDate

internal class MaxElevationInADayStatistic(
    activities: List<Activity>
) : Statistic(name = "Max elevation gain in a day", activities) {

    private val mostActiveDay: Map.Entry<String, Double>? =
        activities.groupBy { activity -> activity.startDateLocal.substringBefore('T') }
            .mapValues { (_, activities) -> activities.sumOf { activity -> activity.totalElevationGain } }
            .maxByOrNull { it.value }

    override val value: String
        get() = if (mostActiveDay != null) {
            "%.2f m - ${LocalDate.parse(mostActiveDay.key).format(dateFormatter)}".format(mostActiveDay.value)
        } else {
            "Not available"
        }
}