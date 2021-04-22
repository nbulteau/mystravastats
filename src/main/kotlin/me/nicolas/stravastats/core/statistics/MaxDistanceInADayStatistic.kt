package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.helpers.dateFormatter
import me.nicolas.stravastats.helpers.inDateTimeFormatter
import java.time.LocalDate
import java.time.LocalDateTime

internal class MaxDistanceInADayStatistic(
    activities: List<Activity>
) : Statistic(name = "Max distance in a day", activities) {

    private val mostActiveDay: Map.Entry<String, Double>? =
        activities.groupBy { activity -> activity.startDateLocal.substringBefore('T') }
            .mapValues { (_, activities) -> activities.sumByDouble { activity -> activity.distance } }
            .maxByOrNull { it.value }

    override fun toString(): String {
        return super.toString() + if (mostActiveDay != null) {
            "%.2f km - %s".format(
                mostActiveDay.value.div(1000),
                LocalDate.parse(mostActiveDay.key).format(dateFormatter)
            )
        } else {
            "Not available"
        }
    }
}