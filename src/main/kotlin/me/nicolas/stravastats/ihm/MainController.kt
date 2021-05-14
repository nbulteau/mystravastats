package me.nicolas.stravastats.ihm

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.chart.XYChart
import me.nicolas.stravastats.MyStravaStatsApp
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.service.ActivityHelper
import me.nicolas.stravastats.service.CSVService
import me.nicolas.stravastats.service.ChartsService
import me.nicolas.stravastats.service.StatisticsService
import me.nicolas.stravastats.service.statistics.ActivityStatistic
import me.nicolas.stravastats.service.statistics.Statistic
import tornadofx.Controller
import java.time.LocalDate

class MainController(private val activities: ObservableList<Activity>) : Controller() {

    private val statsService = StatisticsService()

    private val chartsService = ChartsService()

    private val csvService = CSVService()

    fun generateCSV() {
        csvService.exportCSV(
            MyStravaStatsApp.myStravaStatsParameters.clientId,
            activities,
            MyStravaStatsApp.myStravaStatsParameters.filter
        )
    }

    fun generateCharts() {
        chartsService.buildCharts(activities)
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
                    StatisticDisplay(
                        statistic.name,
                        statistic.value,
                        if (statistic.activity != null) statistic.activity.toString() else ""
                    )
                }
                else -> StatisticDisplay(statistic.name, statistic.toString(), "")
            }
        }
        return FXCollections.observableArrayList(activityStatistics)
    }
}
