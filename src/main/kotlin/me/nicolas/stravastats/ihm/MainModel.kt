package me.nicolas.stravastats.ihm

import javafx.scene.control.Hyperlink

data class StatisticDisplay(val label: String, val value: String, val activity: Hyperlink?)
data class ActivityDisplay(val activity: Hyperlink?, val distance: Double, val totalElevationGain: Double, val date: String)