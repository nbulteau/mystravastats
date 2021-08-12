package me.nicolas.stravastats.service

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Commute
import me.nicolas.stravastats.business.Ride
import me.nicolas.stravastats.business.Run
import me.nicolas.stravastats.business.badges.*
import me.nicolas.stravastats.service.statistics.GlobalStatistic
import me.nicolas.stravastats.service.statistics.MaxStreakStatistic
import me.nicolas.stravastats.service.statistics.MostActiveMonthStatistic

class BadgeService {

    fun computeLocationRideBadges(activities: List<Activity>): List<Triple<Badge, Activity?, Boolean>> {

        val filteredActivities = activities.filter { activity -> activity.stream?.latitudeLongitude != null }

        return checkBadges(LocationBadge.cyclingBadges, filteredActivities)
    }

    fun computeGeneralBadges(
        activityType: String,
        activities: List<Activity>
    ): List<Triple<Badge, Activity?, Boolean>> {

        val filteredActivities = activities.filter { activity -> activity.stream?.latitudeLongitude != null }

        val badgesToCheck: List<Badge> = when (activityType) {
            Ride -> DistanceBadge.rideBadges + ElevationBadge.rideBadges + MovingTimeBadge.movingTimeBadges
            Run -> DistanceBadge.runBadges + ElevationBadge.runBadges + MovingTimeBadge.movingTimeBadges

            else -> emptyList()
        }

        return checkBadges(badgesToCheck, filteredActivities)
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
