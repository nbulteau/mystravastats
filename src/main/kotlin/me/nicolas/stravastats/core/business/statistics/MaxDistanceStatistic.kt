package me.nicolas.stravastats.core.business.statistics

import me.nicolas.stravastats.infrastructure.dao.Activity

internal class MaxDistanceStatistic(
    activities: List<Activity>
) : ActivityStatistic("Max distance", activities) {

    init {
        activity = activities.maxByOrNull { it.distance }
    }

    override fun toString(): String {
        return super.toString() + if (activity != null) {
            " : %.2f km".format(activity?.distance?.div(1000))
        } else {
            " Not available"
        }
    }
}