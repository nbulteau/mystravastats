package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.ActivityEffort
import me.nicolas.stravastats.utils.formatSeconds


internal open class BestEffortDistanceStatistic(
    name: String,
    activities: List<Activity>,
    private val distance: Double
) : ActivityStatistic(name, activities) {

    private val bestActivityEffort = activities
        .mapNotNull { activity -> activity.calculateBestTimeForDistance(distance) }
        .minByOrNull { activityEffort -> activityEffort.seconds }

    init {
        require(distance > 100) { "Distance must be > 100 meters" }
        activity = bestActivityEffort?.activity
    }

    override val value: String
        get() = if (bestActivityEffort != null) {
            "${bestActivityEffort.seconds.formatSeconds()} => ${bestActivityEffort.getFormattedSpeed()}"
        } else {
            "Not available"
        }
}

/**
 * Sliding window best time for a given distance.
 * @param distance given distance.
 */
fun Activity.calculateBestTimeForDistance(distance: Double): ActivityEffort? {

    // no stream -> return null
    if (stream == null || stream?.altitude == null) {
        return null
    }

    var idxStart = 0
    var idxEnd = 0
    var bestTime = Double.MAX_VALUE
    var bestEffort: ActivityEffort? = null

    val distances = this.stream?.distance?.data!!
    val times = this.stream?.time?.data!!
    val altitudes = this.stream?.altitude?.data!!

    val streamDataSize = this.stream?.distance?.originalSize!!

    do {
        val totalDistance = distances[idxEnd] - distances[idxStart]
        val totalAltitude = if (altitudes.isNotEmpty()) {
            altitudes[idxEnd] - altitudes[idxStart]
        } else {
            0.0
        }
        val totalTime = times[idxEnd] - times[idxStart]

        if (totalDistance < distance - 0.5) { // 999.6 m would count towards 1 km
            ++idxEnd
        } else {
            val estimatedTimeForDistance = distance / totalDistance * totalTime
            // estimatedTimeForDistance > 1 to prevent corrupted data
            if (estimatedTimeForDistance < bestTime && estimatedTimeForDistance > 1) {
                bestTime = estimatedTimeForDistance
                bestEffort = ActivityEffort(this, distance, bestTime.toInt(), totalAltitude)
            }
            ++idxStart
        }
    } while (idxEnd < streamDataSize)

    return bestEffort
}
