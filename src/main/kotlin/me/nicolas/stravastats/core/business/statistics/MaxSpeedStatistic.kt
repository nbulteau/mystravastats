package me.nicolas.stravastats.core.business.statistics

import me.nicolas.stravastats.core.business.formatSeconds
import me.nicolas.stravastats.infrastructure.dao.Activity

internal class MaxSpeedStatistic(
    activities: List<Activity>
) : ActivityStatistic("Max speed", activities) {

    init {
        activity = activities.maxByOrNull { it.maxSpeed }
    }

    override fun toString(): String {

        return super.toString() + if (activity != null) {
            if (activity?.type == "Run") {
                activity?.maxSpeed?.times(1000)?.let { "%s/km".format(it.formatSeconds()) }
            } else {
                "%.02f km/h".format(activity?.maxSpeed?.times(3600)?.div(1000))
            } + activity
        } else {
            " Not available"
        }
    }
}