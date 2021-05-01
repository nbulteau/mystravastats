package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Statistic

/**
 * https://en.wikipedia.org/wiki/Arthur_Eddington#Eddington_number_for_cycling
 */
internal class EddingtonStatistic(
    activities: List<Activity>
) : Statistic("Eddington number", activities) {

    val eddingtonNumber = processEddingtonNumber()

    override fun toString(): String {
        return super.toString() + "$eddingtonNumber km"
    }

    private fun processEddingtonNumber(): Int {

        var eddingtonNumber = 0

        if (activities.isNotEmpty()) {

            val activeDaysList = activities
                .groupBy { activity -> activity.startDateLocal.substringBefore('T') }
                .mapValues { (_, activities) -> activities.sumOf { activity -> activity.distance / 1000 } }
                .mapValues { entry: Map.Entry<String, Double> -> entry.value.toInt() }
                .toMap()

            val counts = List(activeDaysList.maxOf { it.value }) { 0 }.toMutableList()
            activeDaysList.forEach { entry: Map.Entry<String, Int> ->
                for (day in entry.value downTo 1) {
                    counts[day - 1] += 1
                }
            }

            for (day in counts.size downTo 1) {
                if (counts[day - 1] >= day) {
                    eddingtonNumber = day
                    break
                }
            }
        }

        return eddingtonNumber
    }
}
