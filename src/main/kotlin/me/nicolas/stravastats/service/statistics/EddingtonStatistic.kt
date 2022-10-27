package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.Activity

/**
 * https://en.wikipedia.org/wiki/Arthur_Eddington#Eddington_number_for_cycling
 */
internal class EddingtonStatistic(
    activities: List<Activity>
) : Statistic("Eddington number", activities) {

    val eddingtonNumber: Int

    init {
        eddingtonNumber = processEddingtonNumber()
    }

    // counts = total up the number of days that you have ridden each distance
    private lateinit var counts: MutableList<Int>

    val nbDaysDistanceIsReached: List<Int>
        get() = counts.toList()

    override val value: String
        get() = "$eddingtonNumber km"

    override fun toString() = value

    private fun processEddingtonNumber(): Int {

        var eddingtonNumber = 0

        if (activities.isNotEmpty()) {

            val activeDaysList = activities
                .groupBy { activity -> activity.startDateLocal.substringBefore('T') }
                .mapValues { (_, activities) -> activities.sumOf { activity -> activity.distance / 1000 } }
                .mapValues { entry: Map.Entry<String, Double> -> entry.value.toInt() }
                .toMap()

            counts = // init to 0
                List(activeDaysList.maxOf { entry: Map.Entry<String, Int> -> entry.value }) { 0 }.toMutableList()

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
        } else {
            counts = mutableListOf()
        }

        return eddingtonNumber
    }
}
