package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.Activity
import java.time.LocalDate

internal class MaxStreakStatistic(
    activities: List<Activity>
) : Statistic("Max streak", activities) {

    private val maxStreak: Int

    init {
        val activeDaysSet = activities
            .map { activity -> LocalDate.parse(activity.startDateLocal.substringBefore('T')).dayOfYear }
            .toSet()

        val activeDays = Array(365) { activeDaysSet.contains(it) }

        var maxLen = 0
        var currLen = 0

        for (k in 0 until 365) {
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

