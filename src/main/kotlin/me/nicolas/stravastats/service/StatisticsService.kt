package me.nicolas.stravastats.service

import me.nicolas.stravastats.business.*
import me.nicolas.stravastats.service.statistics.*


internal class StatisticsService {

    /**
     * Compute statistics.
     * @param activities activities to scan.
     */
    fun computeStatistics(activities: List<Activity>): StravaStatistics {

        val globalStatistics = computeGlobalStats(activities)

        val commuteRideStats =
            computeCommuteBikeStats(activities.filter { activity -> activity.type == Ride && activity.commute })
        val rideStats = computeBikeStats(activities.filter { activity -> activity.type == Ride && !activity.commute })
        val runsStats = computeRunStats(activities.filter { activity -> activity.type == Run })
        val hikesStats = computeHikeStats(activities.filter { activity -> activity.type == Hike })
        val inlineSkate = computeInlineSkateStats(activities.filter { activity -> activity.type == InlineSkate })

        return StravaStatistics(globalStatistics, commuteRideStats, rideStats, runsStats, hikesStats, inlineSkate)
    }

    private fun computeGlobalStats(activities: List<Activity>): List<Statistic> {

        return listOf(
            GlobalStatistic("Nb activities", activities, "%d", List<Activity>::size),
            GlobalStatistic("Nb actives days", activities, "%d") {
                activities
                    .map { activity -> activity.startDateLocal.substringBefore('T') }
                    .toSet()
                    .size
            },
            MaxStreakStatistic(activities),
            MostActiveMonthStatistic(activities),
        )
    }

    private fun computeCommonStats(activities: List<Activity>): List<Statistic> {

        return listOf(
            GlobalStatistic("Nb activities", activities, "%d", List<Activity>::size),

            GlobalStatistic("Nb actives days", activities, "%d") {
                activities
                    .groupBy { activity: Activity -> activity.startDateLocal.substringBefore('T') }
                    .count()
            },
            MaxStreakStatistic(activities),

            GlobalStatistic("Total distance", activities, "%.2f km") {
                activities.sumOf { activity: Activity -> activity.distance } / 1000
            },

            GlobalStatistic("Total elevation", activities, "%.2f m") {
                activities.sumOf { activity: Activity -> activity.totalElevationGain }
            },

            MaxDistanceStatistic(activities),
            MaxDistanceInADayStatistic(activities),

            MaxElevationStatistic(activities),
            MaxElevationInADayStatistic(activities),

            MaxMovingTimeStatistic(activities),
            MostActiveMonthStatistic(activities),
            EddingtonStatistic(activities),
        )
    }

    private fun computeRunStats(activities: List<Activity>): List<Statistic> {

        val statistics = computeCommonStats(activities).toMutableList()

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

    private fun computeBikeStats(activities: List<Activity>): List<Statistic> {

        val statistics = computeCommonStats(activities).toMutableList()
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
                BestElevationDistanceStatistic("Max gradient for 250 m", activities, 250.0),
                BestElevationDistanceStatistic("Max gradient for 500 m", activities, 500.0),
                BestElevationDistanceStatistic("Max gradient for 1000 m", activities, 1000.0),
                BestElevationDistanceStatistic("Max gradient for 5 km", activities, 5000.0),
                BestElevationDistanceStatistic("Max gradient for 10 km", activities, 10000.0),
                BestElevationDistanceStatistic("Max gradient for 20 km", activities, 20000.0),
            )
        )
        return statistics
    }

    private fun computeCommuteBikeStats(activities: List<Activity>): List<Statistic> {

        val statistics = computeCommonStats(activities).toMutableList()
        statistics.addAll(
            listOf(
                MaxSpeedStatistic(activities),
                MaxMovingTimeStatistic(activities),
                BestEffortDistanceStatistic("Best 250 m", activities, 250.0),
                BestEffortDistanceStatistic("Best 500 m", activities, 500.0),
                BestEffortDistanceStatistic("Best 1000 m", activities, 1000.0),
                BestEffortDistanceStatistic("Best 5 km", activities, 5000.0),
                BestEffortDistanceStatistic("Best 10 km", activities, 10000.0),
                BestEffortTimeStatistic("Best 30 min", activities, 30 * 60),
                BestEffortTimeStatistic("Best 1 h", activities, 60 * 60),
                BestElevationDistanceStatistic("Max gradient for 250 m", activities, 250.0),
                BestElevationDistanceStatistic("Max gradient for 500 m", activities, 500.0),
                BestElevationDistanceStatistic("Max gradient for 1000 m", activities, 1000.0),
            )
        )
        return statistics
    }

    private fun computeHikeStats(activities: List<Activity>): List<Statistic> {

        val statistics = computeCommonStats(activities).toMutableList()

        statistics.addAll(
            listOf(
                BestDayStatistic("Max distance in a day", activities, "%s => %.02f km")
                {
                    activities
                        .groupBy { activity: Activity -> activity.startDateLocal.substringBefore('T') }
                        .mapValues { it.value.sumOf { activity -> activity.distance / 1000 } }
                        .maxByOrNull { entry: Map.Entry<String, Double> -> entry.value }
                        ?.toPair()
                },
                BestDayStatistic("Max elevation in a day", activities, "%s => %.02f m")
                {
                    activities
                        .groupBy { activity: Activity -> activity.startDateLocal.substringBefore('T') }
                        .mapValues { it.value.sumOf { activity -> activity.totalElevationGain } }
                        .maxByOrNull { entry: Map.Entry<String, Double> -> entry.value }
                        ?.toPair()
                }
            )
        )

        return statistics
    }

    private fun computeInlineSkateStats(activities: List<Activity>): List<Statistic> {

        val statistics = computeCommonStats(activities).toMutableList()

        statistics.addAll(
            listOf(
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
            )
        )

        return statistics
    }
}