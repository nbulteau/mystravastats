package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.Activity

internal class HighestPointStatistic(
    activities: List<Activity>
) : ActivityStatistic("Highest point", activities) {

    init {
        activity = activities.maxByOrNull { activity -> activity.elevHigh }
    }

    override val value: String
        get() = if (activity != null) {
            "%.2f m".format(activity!!.elevHigh)
        } else {
            "Not available"
        }
}