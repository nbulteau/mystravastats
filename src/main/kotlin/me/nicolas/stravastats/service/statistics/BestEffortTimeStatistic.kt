package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.ActivityEffort


internal open class BestEffortTimeStatistic(
    name: String,
    activities: List<Activity>,
    private val seconds: Int
) : ActivityStatistic(name, activities) {

    private val bestActivityEffort = activities
        .mapNotNull { activity -> activity.calculateBestDistanceForTime(seconds) }
        .maxByOrNull { activityEffort -> activityEffort.distance }

    init {
        require(seconds > 10) { "Distance must be > 10 seconds" }
        activity = bestActivityEffort?.activity
    }

    override val value: String
        get() = if (bestActivityEffort != null) {
            if (bestActivityEffort.distance > 1000) {
                "%.2f km => ${bestActivityEffort.getFormattedSpeed()}".format(bestActivityEffort.distance / 1000)
            } else {
                "%.0f m => ${bestActivityEffort.getFormattedSpeed()}".format(bestActivityEffort.distance)
            }
        } else {
            "Not available"
        }

    protected open fun result(bestActivityEffort: ActivityEffort) =
        if (bestActivityEffort.distance > 1000) {
            "%.2f km => ${bestActivityEffort.getFormattedSpeed()}".format(bestActivityEffort.distance / 1000)
        } else {
            "%.0f m => ${bestActivityEffort.getFormattedSpeed()}".format(bestActivityEffort.distance)
        }
}

/**
 * Sliding window best distance for a given time
 * @param seconds given time
 */
fun Activity.calculateBestDistanceForTime(seconds: Int): ActivityEffort? {

    // no stream -> return null
    if (stream == null || stream?.distance == null || stream?.time == null || stream?.altitude == null) {
        return null
    }

    var idxStart = 0
    var idxEnd = 0
    var maxDist = 0.0
    var bestEffort: ActivityEffort? = null

    val distances = this.stream?.distance?.data!!
    val times = this.stream?.time?.data!!
    val altitudes = this.stream?.altitude?.data!!

    val streamDataSize = distances.size

    do {
        val totalDistance = distances[idxEnd] - distances[idxStart]
        val totalAltitude = altitudes[idxEnd] - altitudes[idxStart]
        val totalTime = times[idxEnd] - times[idxStart]

        if (totalTime < seconds) {
            ++idxEnd
        } else {
            val estimatedDistanceForTime = totalDistance / totalTime * seconds

            if (estimatedDistanceForTime > maxDist) {
                maxDist = estimatedDistanceForTime
                bestEffort = ActivityEffort(this, maxDist, seconds, totalAltitude)
            }
            ++idxStart
        }
    } while (idxEnd < streamDataSize)

    return bestEffort
}
