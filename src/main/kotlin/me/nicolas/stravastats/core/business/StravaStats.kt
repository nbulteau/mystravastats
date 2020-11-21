package me.nicolas.stravastats.core.business

data class StravaStats(
    val commuteRideStats: Stats,
    val sportRideStats: Stats,
    val runsStats: Stats,
    val hikesStats: Stats
) {

    fun displayStatistics() {

        println("Rides (commute)")
        println(commuteRideStats)

        println("Rides (sport)")
        println(sportRideStats)

        println("Runs")
        println(runsStats)

        println("Hikes")
        println(hikesStats)
    }
}
