package me.nicolas.stravastats.business

import me.nicolas.stravastats.service.statistics.Statistic

internal data class StravaStatistics(
    val globalStatistics: List<Statistic>,
    val commuteRideStatistics: List<Statistic>,
    val sportRideStatistics: List<Statistic>,
    val runStatistics: List<Statistic>,
    val hikeStatistics: List<Statistic>,
    val inlineSkateStats: List<Statistic>,
) {

    override fun toString(): String {
        val result = StringBuilder("\n\n* Statistics\n\n")

        result.append(globalStatistics.joinToString("\n", "\n** Overview\n", "\n")
        { statistic -> "${statistic.name.padEnd(30)} : $statistic" })

        result.append(commuteRideStatistics.joinToString("\n", "\n** Rides (commute)\n", "\n")
        { statistic -> "${statistic.name.padEnd(30)} : $statistic" })

        result.append(sportRideStatistics.joinToString("\n", "\n** Rides (sport)\n", "\n")
        { statistic -> "${statistic.name.padEnd(30)} : $statistic" })

        result.append(runStatistics.joinToString("\n", "\n** Runs\n", "\n")
        { statistic -> "${statistic.name.padEnd(30)} : $statistic" })

        result.append(inlineSkateStats.joinToString("\n", "\n** InlineSkate\n", "\n")
        { statistic -> "${statistic.name.padEnd(30)} : $statistic" })

        result.append(hikeStatistics.joinToString("\n", "\n** Hikes\n", "\n")
        { statistic -> "${statistic.name.padEnd(30)} : $statistic" })

        result.append("\n")

        return result.toString()
    }
}