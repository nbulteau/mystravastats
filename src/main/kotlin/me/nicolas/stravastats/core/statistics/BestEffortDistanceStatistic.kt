package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.ActivityEffort
import me.nicolas.stravastats.business.formatSeconds


internal open class BestEffortDistanceStatistic(
    name: String,
    activities: List<Activity>,
    private val distance: Double
) : ActivityStatistic(name, activities) {

    private val bestActivityEffort = activities
        .mapNotNull { calculateBestEffort(it) }
        .minByOrNull { it.seconds }

    init {
        require(distance > 100) { "Distance must be > 100 meters" }
        activity = bestActivityEffort?.activity
    }

    /**
     * Sliding window best effort for a given distance.
     * @param activity Activity to scan.
     */
    private fun calculateBestEffort(activity: Activity): ActivityEffort? {

        var idxStart = 0
        var idxEnd = 0
        var bestTime = Double.MAX_VALUE
        var bestEffort: ActivityEffort? = null

        val distances = activity.stream?.distance?.data!!
        val times = activity.stream?.time?.data!!
        val altitudes = activity.stream?.altitude?.data!!

        val streamDataSize = activity.stream?.distance?.originalSize!!

        do {
            val totalDistance = distances[idxEnd] - distances[idxStart]
            val totalAltitude = altitudes[idxEnd] - altitudes[idxStart]
            val totalTime = times[idxEnd] - times[idxStart]

            if (totalDistance < distance - 0.5) { // 999.6 m would count towards 1 km
                ++idxEnd
            } else {
                val estimatedTimeForDistance = distance / totalDistance * totalTime
                // estimatedTimeForDistance > 1 to prevent corrupted data
                if (estimatedTimeForDistance < bestTime && estimatedTimeForDistance > 1) {
                    bestTime = estimatedTimeForDistance
                    bestEffort = ActivityEffort(activity, distance, bestTime.toInt(), totalAltitude)
                }
                ++idxStart
            }
        } while (idxEnd < streamDataSize)

        return bestEffort
    }

    override fun toString() =
        super.toString() + if (bestActivityEffort != null) {
            "%s => %s%s".format(
                bestActivityEffort.seconds.formatSeconds(),
                bestActivityEffort.getFormattedSpeed(),
                bestActivityEffort.activity
            )
        } else {
            "Not available"
        }
}