package me.nicolas.stravastats.core.business.statistics

import me.nicolas.stravastats.infrastructure.dao.Activity

internal class MaxSpeedStatistic(
    activities: List<Activity>
) : ActivityStatistic("Max speed", activities) {

    init {
        activity = activities.maxByOrNull { it.maxSpeed }
    }

    override fun toString(): String {

        return super.toString() + if (activity != null) {
            "%.02f km/h%s".format(activity?.maxSpeed, activity)
        } else {
            " Not available"
        }
    }
}