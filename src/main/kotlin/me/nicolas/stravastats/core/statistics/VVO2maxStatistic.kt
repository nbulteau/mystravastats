package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.ActivityEffort

/**
 * A [BestEffortTimeStatistic] for 6 minutes that also reports vVO2max.
 *
 * vVO2max: Velocity at maximal oxygen uptake.
 */
internal class VVO2maxStatistic(
    activities: List<Activity>
) : BestEffortTimeStatistic("Best vVO2max (6 min)", activities, 6 * 60) {

    override fun result(bestActivityEffort: ActivityEffort) =
        super.result(bestActivityEffort) +
                " -- vVO2max = %.2f km/h".format(
                    calculateVVO2max(
                        bestActivityEffort.distance,
                        bestActivityEffort.seconds
                    )
                )

    private fun calculateVVO2max(distance: Double, seconds: Int) = distance / seconds * 3600 / 1000
}