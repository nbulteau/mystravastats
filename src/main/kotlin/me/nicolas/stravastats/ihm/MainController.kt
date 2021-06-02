package me.nicolas.stravastats.ihm

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.chart.XYChart
import javafx.scene.control.Hyperlink
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.service.ActivityHelper
import me.nicolas.stravastats.service.CSVService
import me.nicolas.stravastats.service.ChartsService
import me.nicolas.stravastats.service.StatisticsService
import me.nicolas.stravastats.service.statistics.ActivityStatistic
import me.nicolas.stravastats.service.statistics.Statistic
import tornadofx.Controller
import java.awt.Desktop
import java.net.URI


class MainController(private val clientId: String, private val activities: ObservableList<Activity>) : Controller() {

    private val statsService = StatisticsService()

    private val chartsService = ChartsService()

    private val csvService = CSVService()

    fun generateCSV(year: Int) {
        csvService.exportCSV(clientId, activities, year)
    }

    fun generateCharts(year: Int) {
        chartsService.buildCharts(activities, year)
    }

    fun getActiveDaysByActivityTypeByYear(activityType: String, year: Int): Map<String, Int> {
        return activities
            .filter { activity -> activity.startDateLocal.subSequence(0, 4).toString().toInt() == year }
            .filter { activity -> activity.type == activityType }
            .groupBy { activity -> activity.startDateLocal.substringBefore('T') }
            .mapValues { (_, activities) -> activities.sumOf { activity -> activity.distance / 1000 } }
            .mapValues { entry -> entry.value.toInt() }
            .toMap()
    }

    fun getActiveDaysByActivityType(activityType: String): Map<String, Int> {
        return activities
            .filter { activity -> activity.type == activityType }
            .groupBy { activity -> activity.startDateLocal.substringBefore('T') }
            .mapValues { (_, activities) -> activities.sumOf { activity -> activity.distance / 1000 } }
            .mapValues { entry -> entry.value.toInt() }
            .toMap()
    }

    fun getActivitiesByYear() = ActivityHelper.groupActivitiesByYear(activities)

    fun getStatisticsToDisplay(year: Int?): StatisticsToDisplay {
        val stravaStatistics = statsService.computeStatistics(activities.filter { activity ->
            activity.startDateLocal.subSequence(0, 4).toString().toInt() == year
        })
        return StatisticsToDisplay(
            buildStatisticsToDisplay(stravaStatistics.globalStatistics),
            buildStatisticsToDisplay(stravaStatistics.sportRideStatistics),
            buildStatisticsToDisplay(stravaStatistics.commuteRideStatistics),
            buildStatisticsToDisplay(stravaStatistics.runStatistics),
            buildStatisticsToDisplay(stravaStatistics.inlineSkateStats),
            buildStatisticsToDisplay(stravaStatistics.hikeStatistics)
        )
    }

    fun buildDistanceByMonthsSeries(type: String, year: Int): ObservableList<XYChart.Data<String, Number>> {
        val activitiesByMonth: Map<String, List<Activity>> =
            ActivityHelper.groupActivitiesByMonth(activities.filter { activity ->
                activity.startDateLocal.subSequence(0, 4).toString().toInt() == year
            })
        val distanceByMonth: Map<String, Double> = ActivityHelper.sumDistanceByType(activitiesByMonth, type)
        return FXCollections.observableList(distanceByMonth.map { entry ->
            XYChart.Data(entry.key, entry.value)
        })
    }

    fun buildDistanceByDaysSeries(type: String, year: Int): ObservableList<XYChart.Data<String, Number>>? {
        val activitiesByDay: Map<String, List<Activity>> =
            ActivityHelper.groupActivitiesByDay(activities.filter { activity ->
                activity.startDateLocal.subSequence(0, 4).toString().toInt() == year
            }, year)
        val distanceByDay: Map<String, Double> = ActivityHelper.sumDistanceByType(activitiesByDay, type)
        return FXCollections.observableList(distanceByDay.map { entry ->
            XYChart.Data(entry.key, entry.value)
        })
    }

    private fun buildStatisticsToDisplay(statistics: List<Statistic>): ObservableList<StatisticDisplay> {
        val activityStatistics = statistics.map { statistic ->
            when (statistic) {
                is ActivityStatistic -> {
                    val hyperlink = if (statistic.activity != null) {
                        Hyperlink(statistic.activity.toString()).apply {
                            onAction = EventHandler {
                                Desktop.getDesktop().browse(URI("http://www.strava.com/activities/${statistic.activity?.id}"))
                            }
                        }
                    } else {
                        null
                    }

                    StatisticDisplay(statistic.name, statistic.value, hyperlink)
                }
                else -> StatisticDisplay(statistic.name, statistic.toString(), null)
            }
        }
        return FXCollections.observableArrayList(activityStatistics)
    }
}
