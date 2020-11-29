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
    fun getSpeed() = activity.speed()

    fun getSlope() = "%.02f %%".format(100 * altitude / distance)
}