package me.nicolas.stravastats.business.badges

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.service.statistics.MaxDistanceStatistic

data class DistanceBadge(
    override val label: String,
    val distance: Int
) : Badge(label) {

    override fun check(activities: List<Activity>): Pair<Activity?, Boolean> {
        val maxDistanceStatistic = MaxDistanceStatistic(activities)

        if (maxDistanceStatistic.activity?.distance!! >= distance) {
            return Pair(maxDistanceStatistic.activity, true)
        }
        return Pair(null, false)
    }

    override fun toString() = label

    companion object {
        val RIDE_LEVEL_1 = DistanceBadge(
            label = "Hit the road 50 km",
            distance = 50000
        )
        val RIDE_LEVEL_2 = DistanceBadge(
            label = "Hit the road 100 km",
            distance = 100000
        )
        val RIDE_LEVEL_3 = DistanceBadge(
            label = "Hit the road 150 km",
            distance = 150000
        )
        val RIDE_LEVEL_4 = DistanceBadge(
            label = "Hit the road 200 km",
            distance = 200000
        )
        val RIDE_LEVEL_5 = DistanceBadge(
            label = "Hit the road 250 km",
            distance = 250000
        )
        val RIDE_LEVEL_6 = DistanceBadge(
            label = "Hit the road 300 km",
            distance = 300000
        )
        val rideBadges = listOf(RIDE_LEVEL_1, RIDE_LEVEL_2, RIDE_LEVEL_3, RIDE_LEVEL_4, RIDE_LEVEL_5, RIDE_LEVEL_6)

        val RUN_LEVEL_1 = DistanceBadge(
            label = "Run that distance 10 km",
            distance = 10000
        )
        val RUN_LEVEL_2 = DistanceBadge(
            label = "Run that distance half Marathon",
            distance = 21097
        )
        val RUN_LEVEL_3 = DistanceBadge(
            label = "Run that distance 30 lm",
            distance = 30000
        )
        val RUN_LEVEL_4 = DistanceBadge(
            label = "Run that distance Marathon",
            distance = 42195
        )
        val runBadges = listOf(RUN_LEVEL_1, RUN_LEVEL_2, RUN_LEVEL_3, RUN_LEVEL_4)
    }
}