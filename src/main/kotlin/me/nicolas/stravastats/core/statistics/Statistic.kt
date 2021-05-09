package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity

abstract class Statistic(val name: String, protected val activities: List<Activity>) {
    abstract val value: String

    override fun toString() = value
}

internal class GlobalStatistic(
    name: String,
    activities: List<Activity>,
    private val formatString: String,
    private val function: (List<Activity>) -> Number
) : Statistic(name, activities) {

    override val value: String
        get() = formatString.format(function(activities))
}

abstract class ActivityStatistic(
    name: String,
    activities: List<Activity>
) : Statistic(name, activities) {

    var activity: Activity? = null

    override fun toString() = if (activity != null) {
        "$value - $activity"
    } else {
        "Not available"
    }
}



