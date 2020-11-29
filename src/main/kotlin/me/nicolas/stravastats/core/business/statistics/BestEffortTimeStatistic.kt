package me.nicolas.stravastats.core.business.statistics

import me.nicolas.stravastats.core.business.ActivityEffort
import me.nicolas.stravastats.infrastructure.dao.Activity


internal open class BestEffortTimeStatistic(
    name: String,
    activities: List<Activity>,
    private val seconds: Int
) : ActivityStatistic(name, activities) {

    private val bestActivityEffort = activities
        .mapNotNull { calculateBestEffort(it) }
        .maxByOrNull { it.distance }

    init {
        activity = bestActivityEffort?.activity
    }

    /**
     * Sliding window best effort for a given time
     * @param activity Activity to scan.
     */
    private fun calculateBestEffort(activity: Activity): ActivityEffort? {
        var idxStart = 0
        var idxEnd = 0
        var maxDist = 0.0
        var bestEffort: ActivityEffort? = null

        val distances = activity.stream?.distance?.data!!
        val times = activity.stream?.time?.data!!
        val altitudes = activity.stream?.altitude?.data!!

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
                    bestEffort = ActivityEffort(activity, maxDist, seconds, totalAltitude)
                }
                ++idxStart
            }
        } while (idxEnd < streamDataSize)

        return bestEffort
    }

    override fun toString(): String =
        super.toString() + result(bestActivityEffort) + if (activity != null) activity else ""

    protected open fun result(bestActivityEffort: ActivityEffort?) =
        if (bestActivityEffort != null) {
            "%.0f m".format(bestActivityEffort.distance) + bestActivityEffort.getSpeed()
        } else {
            "Not available"
        }
}