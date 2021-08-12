package me.nicolas.stravastats.business.badges

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.service.statistics.MaxElevationStatistic

data class ElevationBadge(
    override val name: String,
    override val isCompleted: Boolean = false,
    val totalElevationGain: Int
) : Badge(name, isCompleted) {

    override fun check(activities: List<Activity>): Pair<Activity?, Boolean> {
        val maxElevationStatistic = MaxElevationStatistic(activities)

        if (maxElevationStatistic.activity?.totalElevationGain!! >= totalElevationGain) {
            return Pair(maxElevationStatistic.activity, true)
        }
        return Pair(null, false)
    }

    override fun toString(): String {
        return "$name\n$totalElevationGain m"
    }

    companion object {
        val RIDE_LEVEL_1 = ElevationBadge(
            name = "Ride that climb",
            totalElevationGain = 1000
        )
        val RIDE_LEVEL_2 = ElevationBadge(
            name = "Ride that climb",
            totalElevationGain = 1500
        )
        val RIDE_LEVEL_3 = ElevationBadge(
            name = "Ride that climb",
            totalElevationGain = 2000
        )
        val RIDE_LEVEL_4 = ElevationBadge(
            name = "Ride that climb",
            totalElevationGain = 2500
        )
        val RIDE_LEVEL_5 = ElevationBadge(
            name = "Ride that climb",
            totalElevationGain = 3000
        )
        val RIDE_LEVEL_6 = ElevationBadge(
            name = "Ride that climb",
            totalElevationGain = 3500
        )
        val rideBadges = listOf(RIDE_LEVEL_1, RIDE_LEVEL_2, RIDE_LEVEL_3, RIDE_LEVEL_4, RIDE_LEVEL_5, RIDE_LEVEL_6)

        val RUN_LEVEL_1 = ElevationBadge(
            name = "Run that climb",
            totalElevationGain = 250
        )
        val RUN_LEVEL_2 = ElevationBadge(
            name = "Run that climb",
            totalElevationGain = 500
        )
        val RUN_LEVEL_3 = ElevationBadge(
            name = "Run that climb",
            totalElevationGain = 1000
        )
        val RUN_LEVEL_4 = ElevationBadge(
            name = "Run that climb",
            totalElevationGain = 1500
        )
        val RUN_LEVEL_5 = ElevationBadge(
            name = "Run that climb",
            totalElevationGain = 2000
        )

        val runBadges = listOf(RUN_LEVEL_1, RUN_LEVEL_2, RUN_LEVEL_3, RUN_LEVEL_4, RUN_LEVEL_5)
    }

}