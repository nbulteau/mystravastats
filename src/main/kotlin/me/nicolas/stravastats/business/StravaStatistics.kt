package me.nicolas.stravastats.business

import me.nicolas.stravastats.core.statistics.Statistic

internal data class StravaStatistics(
    val globalStatistic: List<Statistic>,
    val commuteRideStats: List<Statistic>,
    val sportRideStats: List<Statistic>,
    val runsStats: List<Statistic>,
    val hikesStats: List<Statistic>,
    val inlineSkateStats: List<Statistic>,
)