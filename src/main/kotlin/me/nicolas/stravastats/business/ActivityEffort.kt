package me.nicolas.stravastats.business

import me.nicolas.stravastats.core.formatSeconds


/**
 * An effort within an activity.
 */
data class ActivityEffort(
    val activity: Activity,
    val distance: Double,
    val seconds: Int,
    val altitude: Double,
) {
    fun getFormattedSpeed(): String {
        return if (activity.type == "Run") {
            "%s/km".format((seconds * 1000 / distance).formatSeconds())
        } else {
            "%.02f km/h".format(distance / seconds * 3600 / 1000)
        }
    }

    fun getSpeed(): String {
        return if (activity.type == "Run") {
            "%s".format((seconds * 1000 / distance).formatSeconds())
        } else {
            "%.02f".format(distance / seconds * 3600 / 1000)
        }
    }

    fun getFormattedSlope() = "%.02f %%".format(100 * altitude / distance)

    fun getSlope() = "%.02f".format(100 * altitude / distance)
}