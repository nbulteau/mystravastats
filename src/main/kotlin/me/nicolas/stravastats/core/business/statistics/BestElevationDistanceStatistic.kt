package me.nicolas.stravastats.core.business.statistics

import me.nicolas.stravastats.core.business.ActivityEffort
import me.nicolas.stravastats.core.business.formatDate
import me.nicolas.stravastats.infrastructure.dao.Activity


internal open class BestElevationDistanceStatistic(
    name: String,
    activities: List<Activity>,
    private val distance: Double
) : ActivityStatistic(name, activities) {

    private val bestActivityEffort = activities
        .mapNotNull { calculateBestEffort(it) }
        .maxByOrNull { it.altitude }

    init {
        activity = bestActivityEffort?.activity
    }

    /**
     * Sliding window best elevation gain for a given distance.
     * @param activity Activity to scan.
     */
    private fun calculateBestEffort(activity: Activity): ActivityEffort? {

        var idxStart = 0
        var idxEnd = 0
        var bestElevation = Double.MIN_VALUE
        var bestEffort: ActivityEffort? = null

        val distances = activity.stream?.distance?.data!!
        val times = activity.stream?.time?.data!!
        val altitudes = activity.stream?.altitude?.data!!

        val streamDataSize = activity.stream?.distance?.originalSize!!

        do {
            val distStart: Double = distances[idxStart]
            val distEnd: Double = distances[idxEnd]
            val totalDistance = distEnd - distStart

            val altitudeStart: Double = altitudes[idxStart]
            val altitudeEnd: Double = altitudes[idxEnd]
            val totalAltitude = altitudeEnd - altitudeStart

            val timeStart: Int = times[idxStart]
            val timeEnd: Int = times[idxEnd]
            val totalTime = timeEnd - timeStart

            if (totalDistance < distance - 0.5) { // 999.6 m will count towards 1 km
                ++idxEnd
            } else {
                if (totalAltitude > bestElevation) {
                    bestElevation = totalAltitude
                    bestEffort = ActivityEffort(activity, distance, totalTime, bestElevation)
                }
                ++idxStart
            }
        } while (idxEnd < streamDataSize)

        return bestEffort
    }

    override fun toString(): String {
        val slope = bestActivityEffort?.getSlope()

        return super.toString() +
                if (slope != null) {
                    slope + if (activity != null) {
                        " - ${activity?.name} (${activity?.startDateLocal?.formatDate()})"
                    } else {
                        ""
                    }
                } else "Not available"
    }
}