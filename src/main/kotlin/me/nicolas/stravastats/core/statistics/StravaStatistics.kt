package me.nicolas.stravastats.core.statistics

internal data class StravaStatistics(
    val globalStatistic: List<Statistic>,
    val commuteRideStats: List<Statistic>,
    val sportRideStats: List<Statistic>,
    val runsStats: List<Statistic>,
    val hikesStats: List<Statistic>,
) {

    fun displayStatistics() {

        println("* Statistics")

        println()
        println("** Overview")
        globalStatistic.forEach {
            println(it)
        }
        println()
        println("** Rides (commute)")
        commuteRideStats.forEach {
            println(it)
        }
        println()
        println("** Rides (sport)")
        sportRideStats.forEach {
            println(it)
        }
        println()
        println("** Runs")
        runsStats.forEach {
            println(it)
        }
        println()
        println("** Hikes")
        hikesStats.forEach {
            println(it)
        }
        println()
    }
}
