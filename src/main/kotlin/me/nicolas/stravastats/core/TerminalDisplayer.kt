package me.nicolas.stravastats.core

import me.nicolas.stravastats.core.statistics.StravaStatistics

internal class TerminalDisplayer {

    fun displayStatistics(stravaStats: StravaStatistics) {
        println("* Statistics")

        println()
        println("** Overview")
        stravaStats.globalStatistic.forEach { statistic -> println(statistic) }

        println()
        println("** Rides (commute)")
        stravaStats.commuteRideStats.forEach { statistic -> println(statistic) }

        println()
        println("** Rides (sport)")
        stravaStats.sportRideStats.forEach { statistic -> println(statistic) }

        println()
        println("** Runs")
        stravaStats.runsStats.forEach { statistic -> println(statistic) }
        println()

        println("** InlineSkate")
        stravaStats.inlineSkateStats.forEach { statistic -> println(statistic) }
        println()

        println("** Hikes")
        stravaStats.hikesStats.forEach { statistic ->
            println(statistic)
        }
        println()
    }
}