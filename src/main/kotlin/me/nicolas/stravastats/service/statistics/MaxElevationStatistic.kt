package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.Activity

internal class MaxElevationStatistic(
    activities: List<Activity>
) : ActivityStatistic("Max elevation", activities) {

    init {
        activity = activities.maxByOrNull { activity -> activity.totalElevationGain }
    }

    override val value: String
        get() = if (activity != null) {
            "%.2f m".format(activity?.totalElevationGain)
        } else {
            "Not available"
        }
}