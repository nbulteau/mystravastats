package me.nicolas.stravastats.core.charts

import me.nicolas.stravastats.business.Activity

internal abstract class ActivityChart(activities: List<Activity>, val type: String) {

    protected val activitiesByYear: Map<String, List<Activity>>

    init {
        // group by year
        activitiesByYear = activities.filter { activity -> activity.type == type }
            .groupBy { activity ->
                activity.startDateLocal.subSequence(0, 4).toString()
            }.toSortedMap()
    }

    abstract fun build()
}