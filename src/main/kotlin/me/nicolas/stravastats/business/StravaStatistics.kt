package me.nicolas.stravastats.business

internal data class StravaStatistics(
    val globalStatistic: List<Statistic>,
    val commuteRideStats: List<Statistic>,
    val sportRideStats: List<Statistic>,
    val runsStats: List<Statistic>,
    val hikesStats: List<Statistic>,
    val inlineSkateStats: List<Statistic>,
) {

    override fun toString(): String {
        val result = StringBuilder("\n\n* Statistics\n\n")

        result.append(globalStatistic.joinToString("\n", "\n** Overview\n", "\n")
        { statistic -> statistic.toString() })

        result.append(commuteRideStats.joinToString("\n", "\n** Rides (commute)\n", "\n")
        { statistic -> statistic.toString() })

        result.append(sportRideStats.joinToString("\n", "\n** Rides (sport)\n", "\n")
        { statistic -> statistic.toString() })

        result.append(runsStats.joinToString("\n", "\n** Runs\n", "\n")
        { statistic -> statistic.toString() })

        result.append(inlineSkateStats.joinToString("\n", "\n** InlineSkate\n", "\n")
        { statistic -> statistic.toString() })

        result.append(hikesStats.joinToString("\n", "\n** Hikes\n", "\n")
        { statistic -> statistic.toString() })

        result.append("\n")

        return result.toString()
    }
}