package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.ActivityEffort

/**
 * A {@link BestEffortTimeStatistic} for 12 minutes that is used to report the Cooper test result and VO2 max.
 * Cooper test: distance run in 12 minutes -> VO2 max: maximal oxygen uptake.
 *
 *  It is expressed in milliliters per minute per kilo (ml / min / kg) and can range from 20 to 95 ml / min / kg.
 */
internal class CooperStatistic(
    activities: List<Activity>
) : BestEffortTimeStatistic("Best Cooper (12 min)", activities, 12 * 60) {


    override fun result(bestActivityEffort: ActivityEffort) =
        super.result(bestActivityEffort) +
                " -- VO2 max = %.2f ml/kg/min".format(bestActivityEffort.calculateVo2max())
}

private fun ActivityEffort.calculateVo2max(): Double = (distance - 504.9) / 44.73