package me.nicolas.stravastats.core.business.statistics

import me.nicolas.stravastats.core.business.dateFormatter
import me.nicolas.stravastats.infrastructure.dao.Activity
import java.time.LocalDate

class BestDayStatistic(
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