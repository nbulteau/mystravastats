package me.nicolas.stravastats.business.badges

import me.nicolas.stravastats.business.Activity

sealed class Badge(
    open val label: String,
) {
    abstract fun check(activities: List<Activity>): Pair<Activity?, Boolean>

    override fun toString(): String {
        return label
    }
}



