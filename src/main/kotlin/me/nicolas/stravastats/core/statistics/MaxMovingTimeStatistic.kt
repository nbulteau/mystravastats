package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.core.formatSeconds

internal class MaxMovingTimeStatistic(
    activities: List<Activity>
) : ActivityStatistic("Max moving time", activities) {

    init {
        activity = activities.maxByOrNull { activity -> activity.movingTime }
    }

    override val value: String
        get() = if (activity != null) {
            "${activity?.movingTime?.formatSeconds()}"
        } else {
            "Not available"
        }
}