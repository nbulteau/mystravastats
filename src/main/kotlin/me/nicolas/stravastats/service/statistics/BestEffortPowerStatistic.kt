package me.nicolas.stravastats.service.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.ActivityEffort


internal open class BestEffortPowerStatistic(
    name: String,
    activities: List<Activity>,
    private val seconds: Int
) : ActivityStatistic(name, activities) {

    private val bestActivityEffort = activities
        .mapNotNull { activity -> activity.calculateBestPowerForTime(seconds) }
        .maxByOrNull { activityEffort -> activityEffort.distance }

    init {
        require(seconds > 10) { "Distance must be > 10 seconds" }
        activity = bestActivityEffort?.activity
    }

    override val value: String
        get() = if (bestActivityEffort != null) {
            if (bestActivityEffort.averagePower != null) {
                "%d W".format(bestActivityEffort.averagePower)
            } else {
                "Not available"
            }
        } else {
            "Not available"
        }

    protected open fun result(bestActivityEffort: ActivityEffort) =
        if (bestActivityEffort.averagePower != null) {
            "%d W".format(bestActivityEffort.averagePower)
        } else {
            "Not available"
        }
}

/**
 * Sliding window best power for a given time
 * @param seconds given time
 */
fun Activity.calculateBestPowerForTime(seconds: Int): ActivityEffort? {

    // no stream -> return null
    if (stream == null || stream?.altitude == null) {
        return null
    }

    var idxStart = 0
    var idxEnd = 0
    var maxPower = 0
    var bestEffort: ActivityEffort? = null

    val distances = this.stream?.distance?.data!!
    val times = this.stream?.time?.data!!
    val altitudes = this.stream?.altitude?.data
    val watts = this.stream?.watts?.data

    val streamDataSize = distances.size

    do {
        val totalDistance = distances[idxEnd] - distances[idxStart]
        val totalAltitude = if (altitudes?.isNotEmpty() == true) {
            altitudes[idxEnd] - altitudes[idxStart]
        } else {
            0.0
        }
        val totalPower = if (watts?.isNotEmpty() == true) {
            watts.drop(idxStart).take(idxEnd - idxStart + 1).sum() // sum of all power in the window
        } else {
            0
        }
        val totalTime = times[idxEnd] - times[idxStart]

        if (totalTime < seconds) {
            ++idxEnd
        } else {
            if (totalPower > maxPower) {
                maxPower = totalPower
                val averagePower = totalPower / (idxEnd - idxStart)
                bestEffort = ActivityEffort(this, totalDistance, seconds, totalAltitude, idxStart, idxEnd, averagePower)
            }
            ++idxStart
        }
    } while (idxEnd < streamDataSize)

    return bestEffort
}
