package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.ActivityStatistic
import me.nicolas.stravastats.helpers.formatSeconds

internal class MaxMovingTimeStatistic(
    activities: List<Activity>
) : ActivityStatistic("Max moving time", activities) {

    init {
        activity = activities.maxByOrNull { activity -> activity.movingTime }
    }

    override fun toString(): String {

        return super.toString() + if (activity != null) {
            "%s%s".format(activity?.movingTime?.formatSeconds(), activity)
        } else {
            "Not available"
        }
    }
}