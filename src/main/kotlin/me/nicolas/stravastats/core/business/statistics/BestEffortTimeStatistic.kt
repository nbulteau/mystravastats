package me.nicolas.stravastats.core.business.statistics

import me.nicolas.stravastats.core.business.ActivityEffort
import me.nicolas.stravastats.core.business.formatDate
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

        val distance = activity.stream?.distance?.data!!
        val time = activity.stream?.time?.data!!
        val streamDataSize = distance.size

        do {
            val distStart: Double = distance[idxStart]
            val distEnd: Double = distance[idxEnd]
            val totalDistance = distEnd - distStart
            val timeStart: Int = time[idxStart]
            val timeEnd: Int = time[idxEnd]
            val totalTime = timeEnd - timeStart

            if (totalTime < seconds) {
                ++idxEnd
            } else {
                val estimatedDistanceForTime = totalDistance / totalTime * seconds

                if (estimatedDistanceForTime > maxDist) {
                    maxDist = estimatedDistanceForTime
                    bestEffort =
                        ActivityEffort(activity, maxDist, seconds)
                }
                ++idxStart
            }
        } while (idxEnd < streamDataSize)

        return bestEffort
    }

    override fun toString() =
        super.toString() + result(bestActivityEffort) + if (activity != null) {
            " - ${activity?.name} (${activity?.startDateLocal?.formatDate()})"
        } else {
            ""
        }

    protected open fun result(bestActivityEffort: ActivityEffort?) =
        if (bestActivityEffort != null) {
            "%.0f m".format(bestActivityEffort.distance) + bestActivityEffort
        } else {
            "Not available"
        }
}