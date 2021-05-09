package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.ActivityStatistic

internal class MaxSpeedStatistic(
    activities: List<Activity>
) : ActivityStatistic("Max speed", activities) {

    init {
        activity = activities.maxByOrNull { activity -> activity.maxSpeed }
    }

    override fun display() = if (activity != null) {
        "%.02f km/h%s".format(activity?.maxSpeed?.times(3.6), activity)
    } else {
        " Not available"
    }
}