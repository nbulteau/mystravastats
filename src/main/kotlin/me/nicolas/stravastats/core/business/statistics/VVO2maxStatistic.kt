package me.nicolas.stravastats.core.business.statistics

import me.nicolas.stravastats.infrastructure.dao.Activity

/**
 * A [BestEffortTimeStatistic] for 6 minutes that also reports vVO2max.
 *
 * vVO2max: Velocity at maximal oxygen uptake.
 */
internal class VVO2maxStatistic(
    activities: List<Activity>
) : BestEffortTimeStatistic("Best vVO2max (6 min)", activities, 6 * 60) {

    override fun toString(): String {
        return super.toString() + " -- vVO2max = %.2f km/h".format(
            bestActivityEffort?.distance!! / bestActivityEffort.seconds * 3600 / 1000
        )
    }
}