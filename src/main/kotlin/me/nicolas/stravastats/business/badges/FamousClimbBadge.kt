package me.nicolas.stravastats.business.badges

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.GeoCoordinate

data class FamousClimbBadge(
    override val label: String,
    val name: String,
    val topOfTheAscent: Int,
    val start: GeoCoordinate,
    val end: GeoCoordinate,
    val length: Double,
    val totalAscent: Int,
    val averageGradient: Double,
    val difficulty: Int) : Badge(label) {

    override fun check(activities: List<Activity>): Pair<Activity?, Boolean> {
        val filteredActivities = activities.filter { activity ->
            if (activity.startLatlng?.isNotEmpty() == true) {
                this.start.haversineInKM(activity.startLatlng[0], activity.startLatlng[1]) < 50
            } else {
                false
            }
        }.filter { activity ->
            check(activity, this.start) && check(activity, this.end)
        }

        return if (filteredActivities.isEmpty()) {
            Pair(null, false)
        } else {
            Pair(filteredActivities.last(), true)
        }
    }

    private fun check(activity: Activity, geoCoordinateToCheck: GeoCoordinate): Boolean {
        if(activity.stream != null && activity.stream?.latitudeLongitude != null) {
            for (coords in activity.stream?.latitudeLongitude?.data!!) {
                if (geoCoordinateToCheck.match(coords[0], coords[1])) {
                    return true
                }
            }
        }

        return false
    }

    override fun toString() = name
}

