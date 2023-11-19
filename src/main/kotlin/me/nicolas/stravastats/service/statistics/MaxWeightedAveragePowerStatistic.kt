package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.Activity

/**
 * Weighted Average Power (WAP) is a power calculation that takes into account the variability of your power output.
 */
internal class MaxWeightedAveragePowerStatistic(
    activities: List<Activity>
) : ActivityStatistic("Weighted average power", activities) {

    init {
        activity = activities.maxByOrNull { activity -> activity.weightedAverageWatts }
    }

    override val value: String
        get() = if (activity != null) {
            "%d W".format(activity?.weightedAverageWatts)
        } else {
            "Not available"
        }
}