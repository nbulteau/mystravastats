package me.nicolas.stravastats.business.badges

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.service.statistics.MaxDistanceStatistic
import me.nicolas.stravastats.service.statistics.MaxElevationStatistic
import kotlin.collections.Map

data class ElevationBadge(
    override val name: String,
    override val isCompleted: Boolean = false,
    val totalElevationGain: Int
) : Badge(name, isCompleted) {

    override fun check(activities: List<Activity>): Pair<Activity?, Boolean> {
        val maxElevationStatistic = MaxElevationStatistic(activities)

        if (maxElevationStatistic.activity?.totalElevationGain!!  >= totalElevationGain) {
            return Pair(maxElevationStatistic.activity, true)
        }
        return Pair(null, false)
    }

    override fun toString(): String {
        return "$name\n$totalElevationGain m"
    }

    companion object {
        val LEVEL_1 = ElevationBadge(
            name = "Ride that climb",
            totalElevationGain = 1000
        )
        val LEVEL_2 = ElevationBadge(
            name = "Ride that climb",
            totalElevationGain = 1500
        )
        val LEVEL_3 = ElevationBadge(
            name = "Ride that climb",
            totalElevationGain = 2000
        )
        val LEVEL_4 = ElevationBadge(
            name = "Ride that climb",
            totalElevationGain = 2500
        )
        val LEVEL_5 = ElevationBadge(
            name = "Ride that climb",
            totalElevationGain = 3000
        )
        val LEVEL_6 = ElevationBadge(
            name = "Ride that climb",
            totalElevationGain = 3500
        )
        val cyclingBadges = listOf(LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5, LEVEL_6)
    }

}