package me.nicolas.stravastats.core.business

data class Stats(val count: Int, val totalDistance: Double, val totalElevationGain: Double) {

    override fun toString(): String {
        return "=> count = $count\n" +
                "=> total distance = %.2f km\n".format(totalDistance) +
                "=> total elevation gain = %.2f m".format(totalElevationGain)
    }
}
