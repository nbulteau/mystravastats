package me.nicolas.stravastats.service

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Ride
import me.nicolas.stravastats.business.badges.Badge
import me.nicolas.stravastats.business.badges.DistanceBadge
import me.nicolas.stravastats.business.badges.ElevationBadge
import me.nicolas.stravastats.business.badges.LocationBadge

class BadgeService {

    fun computeBadges(activityType: String, activities: List<Activity>): List<Triple<Badge, Activity?, Boolean>> {

        val badges = mutableListOf<Triple<Badge, Activity?, Boolean>>()

        val filteredActivities = activities.filter { activity -> activity.stream?.latitudeLongitude != null }

        when (activityType) {
            Ride -> {
                val badgesToCheck =
                    LocationBadge.cyclingBadges + DistanceBadge.cyclingBadges + ElevationBadge.cyclingBadges
                badges.addAll(checkBadges(badgesToCheck, filteredActivities))
            }
        }

        return badges
    }

    private fun checkBadges(
        badgesToCheck: List<Badge>,
        activities: List<Activity>
    ): List<Triple<Badge, Activity?, Boolean>> {
        val badges = mutableListOf<Triple<Badge, Activity?, Boolean>>()

        for (badgeToCheck in badgesToCheck) {
            val (activity, isCompleted) = badgeToCheck.check(activities)
            badges.add(Triple(badgeToCheck, activity, isCompleted))
        }

        return badges
    }

}
