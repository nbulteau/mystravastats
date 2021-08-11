package me.nicolas.stravastats.business.badges

import me.nicolas.stravastats.business.Activity
import kotlin.collections.Map

abstract class Badge(
    open val name: String,
    open val isCompleted: Boolean = false
) {
    abstract fun check(activities: List<Activity>): Pair<Activity?, Boolean>
}

data class TimeBadge(
    override val name: String,
    override val isCompleted: Boolean = false,
    val levels: Map<String, Double>
) : Badge(name, isCompleted) {
    override fun check(activities: List<Activity>): Pair<Activity?, Boolean> {
        TODO("Not yet implemented")
    }

}

