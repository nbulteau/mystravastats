package me.nicolas.stravastats.ihm

import javafx.collections.ObservableList
import javafx.scene.control.Hyperlink

data class StatisticsToDisplay(
    val globalStatistics: ObservableList<StatisticDisplay>,
    val sportRideStatistics: ObservableList<StatisticDisplay>,
    val commuteRideStatistics: ObservableList<StatisticDisplay>,
    val runStatistics: ObservableList<StatisticDisplay>,
    val inlineSkateStatistics: ObservableList<StatisticDisplay>,
    val hikeStatics: ObservableList<StatisticDisplay>
)

data class StatisticDisplay(val label: String, val value: String, val activity: Hyperlink?)