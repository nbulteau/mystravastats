package me.nicolas.stravastats.business.badges

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.service.statistics.MaxDistanceStatistic

data class DistanceBadge(
    override val name: String,
    override val isCompleted: Boolean = false,
    val distance: Int
) : Badge(name, isCompleted) {

    override fun toString(): String {
        return "$name\n$distance km"
    }

    override fun check(activities: List<Activity>): Pair<Activity?, Boolean> {
        val maxDistanceStatistic = MaxDistanceStatistic(activities)

        if (maxDistanceStatistic.activity?.distance!! / 1000 >= distance) {
            return Pair(maxDistanceStatistic.activity, true)
        }
        return Pair(null, false)
    }

    companion object {
        val LEVEL_1 = DistanceBadge(
            name = "Hit the road",
            distance = 50
        )
        val LEVEL_2 = DistanceBadge(
            name = "Hit the road",
            distance = 100
        )
        val LEVEL_3 = DistanceBadge(
            name = "Hit the road",
            distance = 150
        )
        val LEVEL_4 = DistanceBadge(
            name = "Hit the road",
            distance = 200
        )
        val LEVEL_5 = DistanceBadge(
            name = "Hit the road",
            distance = 250
        )
        val LEVEL_6 = DistanceBadge(
            name = "Hit the road",
            distance = 300
        )
        val cyclingBadges = listOf(LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5, LEVEL_6)
    }
}