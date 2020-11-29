package me.nicolas.stravastats.core.business.statistics

import me.nicolas.stravastats.infrastructure.dao.Activity

internal class MaxElevationStatistic(
    activities: List<Activity>
) : ActivityStatistic("Max elevation", activities) {

    init {
        activity = activities.maxByOrNull { it.totalElevationGain }
    }

    override fun toString(): String {
        return super.toString() + if (activity != null) {
            "%.2f m".format(activity?.totalElevationGain) + activity
        } else {
            "Not available"
        }
    }
}