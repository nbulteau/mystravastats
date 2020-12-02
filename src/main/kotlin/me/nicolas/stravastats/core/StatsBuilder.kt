package me.nicolas.stravastats.core

import me.nicolas.stravastats.core.business.statistics.*
import me.nicolas.stravastats.infrastructure.dao.Activity


internal class StatsBuilder {

    /**
     * Compute statistics.
     * @param activities
     */
    fun computeGlobalStats(activities: List<Activity>): List<Statistic> {

        return listOf(
            GlobalStatistic("Nb activities", activities, "%d", List<Activity>::size),
            GlobalStatistic("Nb actives days", activities, "%d")
            { activityList: List<Activity> ->
                activityList
                    .map { it.startDateLocal.substringBefore('T') }
                    .toSet()
                    .size
            },
            MaxStreakStatistic(activities),
            MostActiveMonthStatistic(activities),
        )
    }

    /**
     * Compute statistics.
     * @param activities
     */
    fun computeStats(activities: List<Activity>): List<Statistic> {

        return listOf(
            GlobalStatistic("Nb activities", activities, "%d", List<Activity>::size),

            GlobalStatistic("Nb actives days", activities, "%d")
            { activityList: List<Activity> ->
                activityList
                    .groupBy { it.startDateLocal.substringBefore('T') }
                    .count()
            },
            MaxStreakStatistic(activities),

            GlobalStatistic("Total distance", activities, "%.2f km")
            { activityList: List<Activity> -> activityList.sumByDouble { it.distance } / 1000 },

            GlobalStatistic("Total elevation", activities, "%.2f m")
            { activityList: List<Activity> -> activityList.sumByDouble { it.totalElevationGain } },

            MaxDistanceStatistic(activities),
            MaxElevationStatistic(activities),
            MaxMovingTimeStatistic(activities),
            MostActiveMonthStatistic(activities),
        )
    }

    /**
     * Compute Run statistics.
     * @param activities
     */
    fun computeRunStats(activities: List<Activity>): List<Statistic> {

        val statistics = computeStats(activities).toMutableList()

        statistics.addAll(
            listOf(
                CooperStatistic(activities),
                VVO2maxStatistic(activities),
                BestEffortDistanceStatistic("Best 200 m", activities, 200.0),
                BestEffortDistanceStatistic("Best 400 m", activities, 400.0),
                BestEffortDistanceStatistic("Best 1000 m", activities, 1000.0),
                BestEffortDistanceStatistic("Best 10000 m", activities, 10000.0),
                BestEffortDistanceStatistic("Best half Marathon", activities, 21097.0),
                BestEffortDistanceStatistic("Best Marathon", activities, 42195.0),
                BestEffortTimeStatistic("Best 1 h", activities, 60 * 60),
                BestEffortTimeStatistic("Best 2 h", activities, 2 * 60 * 60),
                BestEffortTimeStatistic("Best 3 h", activities, 3 * 60 * 60),
                BestEffortTimeStatistic("Best 4 h", activities, 4 * 60 * 60),
                BestEffortTimeStatistic("Best 5 h", activities, 5 * 60 * 60),
                BestEffortTimeStatistic("Best 6 h", activities, 6 * 60 * 60),
            )
        )

        return statistics
    }

    /**
     * Compute Bike statistics.
     * @param activities
     */
    fun computeBikeStats(activities: List<Activity>): List<Statistic> {

        val statistics = computeStats(activities).toMutableList()
        statistics.addAll(
            listOf(
                MaxSpeedStatistic(activities),
                MaxMovingTimeStatistic(activities),
                BestEffortDistanceStatistic("Best 250 m", activities, 250.0),
                BestEffortDistanceStatistic("Best 500 m", activities, 500.0),
                BestEffortDistanceStatistic("Best 1000 m", activities, 1000.0),
                BestEffortDistanceStatistic("Best 5 km", activities, 5000.0),
                BestEffortDistanceStatistic("Best 10 km", activities, 10000.0),
                BestEffortDistanceStatistic("Best 20 km", activities, 20000.0),
                BestEffortDistanceStatistic("Best 50 km", activities, 50000.0),
                BestEffortDistanceStatistic("Best 100 km", activities, 100000.0),
                BestEffortTimeStatistic("Best 30 min", activities, 30 * 60),
                BestEffortTimeStatistic("Best 1 h", activities, 60 * 60),
                BestEffortTimeStatistic("Best 2 h", activities, 2 * 60 * 60),
                BestEffortTimeStatistic("Best 3 h", activities, 3 * 60 * 60),
                BestEffortTimeStatistic("Best 4 h", activities, 4 * 60 * 60),
                BestEffortTimeStatistic("Best 5 h", activities, 5 * 60 * 60),
                BestElevationDistanceStatistic("Max slope for 250 m", activities, 250.0),
                BestElevationDistanceStatistic("Max slope for 500 m", activities, 500.0),
                BestElevationDistanceStatistic("Max slope for 1000 m", activities, 1000.0),
                BestElevationDistanceStatistic("Max slope for 5 km", activities, 5000.0),
                BestElevationDistanceStatistic("Max slope for 10 km", activities, 10000.0),
                BestElevationDistanceStatistic("Max slope for 20 km", activities, 20000.0),
            )
        )
        return statistics
    }

    /**
     * Compute Hike statistics.
     * @param activities
     */
    fun computeHikeStats(activities: List<Activity>): List<Statistic> {

        val statistics = computeStats(activities).toMutableList()

        statistics.addAll(
            listOf(
                BestDayStatistic("Max distance in a day", activities, "%s => %.02f km")
                {
                    activities
                        .groupBy { it.startDateLocal.substringBefore('T') }
                        .mapValues { it.value.sumByDouble { activity -> activity.distance / 1000 } }
                        .maxByOrNull { it.value }
                        ?.toPair()
                },
                BestDayStatistic("Max elevation in a day", activities, "%s => %.02f m")
                {
                    activities
                        .groupBy { it.startDateLocal.substringBefore('T') }
                        .mapValues { it.value.sumByDouble { activity -> activity.totalElevationGain } }
                        .maxByOrNull { it.value }
                        ?.toPair()
                }
            )
        )

        return statistics
    }
}