package me.nicolas.stravastats.core.business.statistics

import me.nicolas.stravastats.infrastructure.dao.Activity

abstract class Statistic(
    private val name: String,
    protected val activities: List<Activity>
) {
    override fun toString(): String {
        return name
    }
}

internal class GlobalStatistic(
    name: String,
    activities: List<Activity>,
    private val formatString: String,
    private val function: (List<Activity>) -> Number
) : Statistic(name, activities) {

    override fun toString(): String {
        return super.toString() + formatString.format(function(activities))
    }
}

internal abstract class ActivityStatistic(
    name: String,
    activities: List<Activity>
) : Statistic(name, activities) {

    protected var activity: Activity? = null
}



