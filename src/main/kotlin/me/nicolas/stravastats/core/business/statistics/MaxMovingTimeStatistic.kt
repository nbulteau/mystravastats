package me.nicolas.stravastats.core.business.statistics

import me.nicolas.stravastats.core.business.formatSeconds
import me.nicolas.stravastats.infrastructure.dao.Activity

internal class MaxMovingTimeStatistic(
    activities: List<Activity>
) : ActivityStatistic("Max moving time", activities) {

    init {
        activity = activities.maxByOrNull { it.movingTime }
    }

    override fun toString(): String {

        return super.toString() + if (activity != null) {
            "%s%s".format(activity?.movingTime?.formatSeconds(), activity)
        } else {
            "Not available"
        }
    }
}