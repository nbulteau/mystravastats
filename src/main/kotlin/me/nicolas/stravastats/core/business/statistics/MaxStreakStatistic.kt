package me.nicolas.stravastats.core.business.statistics

import me.nicolas.stravastats.infrastructure.dao.Activity
import java.time.LocalDate

internal class MaxStreakStatistic(
    activities: List<Activity>
) : ActivityStatistic("Max streak", activities) {

    private val maxStreak: Int

    init {
        val activeDaysSet = activities
            .map { LocalDate.parse(it.startDateLocal.substringBefore('T')).dayOfYear }
            .toSet()

        val activeDays = Array<Boolean>(365) { activeDaysSet.contains(it) }

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

    override fun toString(): String {
        return super.toString() + maxStreak
    }
}

