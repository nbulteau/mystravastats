package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.Activity

internal class MaxSpeedStatistic(
    activities: List<Activity>
) : ActivityStatistic("Max speed", activities) {

    init {
        activity = activities.maxByOrNull { activity -> activity.maxSpeed }
    }

    override val value: String
        get() = if (activity != null) {
            "%.02f km/h".format(activity?.maxSpeed?.times(3.6))
        } else {
            "Not available"
        }
}