package me.nicolas.stravastats.core.business.statistics

import me.nicolas.stravastats.core.business.ActivityEffort
import me.nicolas.stravastats.infrastructure.dao.Activity

/**
 * A {@link BestEffortTimeStatistic} for 12 minutes that is used to report the Cooper test result and VO2 max.
 * Cooper test: distance run in 12 minutes -> VO2 max: maximal oxygen uptake.
 *
 *  It is expressed in milliliters per minute per kilo (ml / min / kg) and can range from 20 to 95 ml / min / kg.
 */
internal class CooperStatistic(
    activities: List<Activity>
) : BestEffortTimeStatistic("Best Cooper (12 min)", activities, 12 * 60) {


    override fun result(bestActivityEffort: ActivityEffort?) =
        super.result(bestActivityEffort) + if (bestActivityEffort != null) {
            " -- VO2 max = %.2f ml/kg/min".format(calculateVo2max(bestActivityEffort.distance))
        } else {
            " Not available"
        }

    private fun calculateVo2max(distanceIn12Min: Double): Double {
        return (distanceIn12Min - 504.9) / 44.73
    }
}