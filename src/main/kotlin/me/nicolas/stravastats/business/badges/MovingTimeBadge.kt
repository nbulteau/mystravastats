package me.nicolas.stravastats.business.badges

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.service.statistics.MaxMovingTimeStatistic

data class MovingTimeBadge(
    override val label: String,
    val movingTime: Int
) : Badge(label) {

    override fun check(activities: List<Activity>): Pair<Activity?, Boolean> {
        val maxMovingTimeStatistic = MaxMovingTimeStatistic(activities)

        if (maxMovingTimeStatistic.activity?.movingTime!! >= movingTime) {
            return Pair(maxMovingTimeStatistic.activity, true)
        }
        return Pair(null, false)
    }

    override fun toString(): String {
        return label
    }

    companion object {
        val LEVEL_1 = MovingTimeBadge(
            label = "Moving time 1 hour",
            movingTime = 3600
        )
        val LEVEL_2 = MovingTimeBadge(
            label = "Moving time 2 hours",
            movingTime = 7200
        )
        val LEVEL_3 = MovingTimeBadge(
            label = "Moving time 3 hours",
            movingTime = 10800
        )
        val LEVEL_4 = MovingTimeBadge(
            label = "Moving time 4 hours",
            movingTime = 14400
        )
        val LEVEL_5 = MovingTimeBadge(
            label = "Moving time 5 hours",
            movingTime = 18000
        )
        val LEVEL_6 = MovingTimeBadge(
            label = "Moving time 6 hours",
            movingTime = 21600
        )
        val LEVEL_7 = MovingTimeBadge(
            label = "Moving time 7 hours",
            movingTime = 25200
        )
        val movingTimeBadges = listOf(LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5, LEVEL_6, LEVEL_7)
    }
}
