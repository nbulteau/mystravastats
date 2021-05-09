package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.ActivityStatistic
import me.nicolas.stravastats.core.formatSeconds

internal class MaxMovingTimeStatistic(
    activities: List<Activity>
) : ActivityStatistic("Max moving time", activities) {

    init {
        activity = activities.maxByOrNull { activity -> activity.movingTime }
    }

    override fun display() = if (activity != null) {
        "%s%s".format(activity?.movingTime?.formatSeconds(), activity)
    } else {
        "Not available"
    }
}