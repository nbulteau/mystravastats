package me.nicolas.stravastats.core.business.statistics

import me.nicolas.stravastats.infrastructure.dao.Activity
import java.time.LocalDate

/**
 * https://en.wikipedia.org/wiki/Arthur_Eddington#Eddington_number_for_cycling
 */
internal class Eddington(
    activities: List<Activity>
) : Statistic("Eddington number", activities) {

    val eddingtonNumber = processEddingtonNumber()

    override fun toString(): String {
        return super.toString() + eddingtonNumber
    }

    private fun processEddingtonNumber(): Int {

        var eddingtonNumber = 0

        if (activities.isNotEmpty()) {

            val activeDaysList = activities
                .groupBy { LocalDate.parse(it.startDateLocal.substringBefore('T')).dayOfYear }
                .mapValues { (_, activities) -> activities.sumByDouble { it.distance / 1000 } }
                .mapValues { it.value.toInt() }
                .toMap()

            val counts = List(activeDaysList.maxOf { it.value }) { 0 }.toMutableList()
            activeDaysList.forEach {
                for (day in it.value downTo 1) {
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
