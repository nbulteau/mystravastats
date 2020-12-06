package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity

internal class MaxDistanceStatistic(
    activities: List<Activity>
) : ActivityStatistic("Max distance", activities) {

    init {
        activity = activities.maxByOrNull { activity -> activity.distance }
    }

    override fun toString(): String {
        return super.toString() + if (activity != null) {
            "%.2f km".format(activity?.distance?.div(1000)) + activity
        } else {
            "Not available"
        }
    }
}