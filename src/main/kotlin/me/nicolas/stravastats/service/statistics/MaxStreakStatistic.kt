package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.Activity
import java.time.LocalDate

internal class MaxStreakStatistic(
    activities: List<Activity>
) : Statistic("Max streak", activities) {

    private val maxStreak: Int

    init {
        val lastDate = LocalDate.parse(activities.first().startDateLocal.substringBefore('T'))
        val firstDate = LocalDate.parse(activities.last().startDateLocal.substringBefore('T'))
        val firstEpochDay = firstDate.toEpochDay()
        val activeDaysSet: Set<Int> = activities
            .map { activity ->
                val date = LocalDate.parse(activity.startDateLocal.substringBefore('T'))
                (date.toEpochDay() - firstEpochDay).toInt()
            }.toSet()

        val days = (lastDate.toEpochDay() - firstDate.toEpochDay()).toInt()
        val activeDays = Array(days) { activeDaysSet.contains(it) }

        var maxLen = 0
        var currLen = 0

        for (k in 0 until days) {
            if (activeDays[k]) {
                currLen++
            } else {
                if (currLen > maxLen) {
                    maxLen = currLen
                }
                currLen = 0
            }
        }
        maxStreak = maxLen
    }

    override val value: String
        get() = maxStreak.toString()

    override fun toString() = value
}

