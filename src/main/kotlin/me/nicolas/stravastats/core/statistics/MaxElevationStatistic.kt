package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.ActivityStatistic

internal class MaxElevationStatistic(
    activities: List<Activity>
) : ActivityStatistic("Max elevation", activities) {

    init {
        activity = activities.maxByOrNull { activity -> activity.totalElevationGain }
    }

    override fun display() = if (activity != null) {
        "%.2f m".format(activity?.totalElevationGain) + activity
    } else {
        "Not available"
    }
}