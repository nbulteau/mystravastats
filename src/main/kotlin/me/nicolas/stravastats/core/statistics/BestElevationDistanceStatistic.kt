package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.ActivityEffort


internal open class BestElevationDistanceStatistic(
    name: String,
    activities: List<Activity>,
    private val distance: Double
) : ActivityStatistic(name, activities) {

    private val bestActivityEffort = activities
        .mapNotNull { activity -> activity.calculateBestElevationForDistance(distance) }
        .maxByOrNull { activityEffort -> activityEffort.altitude }

    init {
        require(distance > 100) { "Distance must be > 100 meters" }
        activity = bestActivityEffort?.activity
    }

    override fun toString(): String {

        return super.toString() + if (bestActivityEffort != null) {
            bestActivityEffort.getFormattedSlope() + bestActivityEffort.activity
        } else {
            "Not available"
        }
    }
}

/**
 * Sliding window best elevation gain for a given distance.
 * @param distance given distance.
 */
private fun Activity.calculateBestElevationForDistance(distance: Double): ActivityEffort? {

    var idxStart = 0
    var idxEnd = 0
    var bestElevation = Double.MIN_VALUE
    var bestEffort: ActivityEffort? = null

    val distances = this.stream?.distance?.data!!
    val times = this.stream?.time?.data!!
    val altitudes = this.stream?.altitude?.data!!

    val streamDataSize = this.stream?.distance?.originalSize!!

    do {
        val totalDistance = distances[idxEnd] - distances[idxStart]
        val totalAltitude = altitudes[idxEnd] - altitudes[idxStart]
        val totalTime = times[idxEnd] - times[idxStart]

        if (totalDistance < distance - 0.5) { // 999.6 m will count towards 1 km
            ++idxEnd
        } else {
            if (totalAltitude > bestElevation) {
                bestElevation = totalAltitude
                bestEffort = ActivityEffort(this, distance, totalTime, bestElevation)
            }
            ++idxStart
        }
    } while (idxEnd < streamDataSize)

    return bestEffort
}
