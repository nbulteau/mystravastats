package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Statistic
import me.nicolas.stravastats.helpers.dateFormatter
import java.time.LocalDate

internal class BestDayStatistic(
    name: String,
    activities: List<Activity>,
    private val formatString: String,
    private val function: (List<Activity>) -> Pair<String, Number>?
) : Statistic(name, activities) {

    override fun toString(): String {

        val pair = function(activities)
        return super.toString() + if (pair != null) {
            val date = LocalDate.parse(pair.first)
            formatString.format(date.format(dateFormatter), pair.second)
        } else {
            "Not available"
        }
    }
}