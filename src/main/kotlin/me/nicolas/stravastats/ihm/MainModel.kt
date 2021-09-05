package me.nicolas.stravastats.ihm

import javafx.scene.control.Hyperlink
import me.nicolas.stravastats.business.badges.Badge

data class StatisticDisplay(val label: String, val value: String, val activity: Hyperlink?)

data class ActivityDisplay(
    val name: Hyperlink?,
    val distance: Double,
    val elapsedTime: Int,
    val totalElevationGain: Double,
    val averageSpeed: Double,
    val bestTimeForDistanceFor1000m: String,
    val BestElevationForDistanceFor250m: String,
    val date: String
)

data class BadgeDisplay(val label: String, val activity: Hyperlink?, val badge: Badge)