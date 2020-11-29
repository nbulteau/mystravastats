package me.nicolas.stravastats.core.business.statistics

import me.nicolas.stravastats.core.business.ActivityEffort
import me.nicolas.stravastats.core.business.formatSeconds
import me.nicolas.stravastats.infrastructure.dao.Activity


internal open class BestEffortDistanceStatistic(
    name: String,
    activities: List<Activity>,
    private val distance: Double
) : ActivityStatistic(name, activities) {

    private val bestActivityEffort = activities
        .mapNotNull { calculateBestEffort(it) }
        .minByOrNull { it.seconds }

    init {
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
                val estimatedTimeForDistance = totalTime / totalDistance * distance
                if (estimatedTimeForDistance < bestTime) {
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
            "%s m => %s%s".format(bestActivityEffort.seconds.formatSeconds(), activity?.speed(), activity ?: "")
        } else {
            "Not available"
        }
}