package me.nicolas.stravastats.core.business

import me.nicolas.stravastats.infrastructure.dao.Activity


/**
 * An effort within an activity.
 */
internal data class ActivityEffort(
    val activity: Activity,
    val distance: Double,
    val seconds: Int,
    val altitude: Double,
) {
    fun getSpeed(): String {
        return if (activity.type == "Run") {
            "${(seconds * 1000 / distance).formatSeconds()}/km"
        } else {
            "%.02f km/h".format(distance / seconds * 3600 / 1000)
        }
    }

    fun getSlope() = "%.02f %%".format(100 * altitude / distance)
}