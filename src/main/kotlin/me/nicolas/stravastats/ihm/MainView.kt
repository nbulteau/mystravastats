package me.nicolas.stravastats.ihm

import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.ObservableList
import javafx.geometry.Side
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import me.nicolas.stravastats.MyStravaStatsApp
import me.nicolas.stravastats.business.*
import me.nicolas.stravastats.service.ActivityHelper
import tornadofx.*
import java.time.LocalDate


class MainView(athlete: Athlete?, activities: ObservableList<Activity>) : View("MyStravaStats") {
    override val root = borderpane {
        setPrefSize(1024.0, 768.0)
    }

    private val mainController: MainController = MainController(activities)

    private var selectedYear = SimpleIntegerProperty(LocalDate.now().year)

    private var globalStatsTab: Tab by singleAssign()
    private var sportRideStatsTab: Tab by singleAssign()
    private var commuteRideStatsTab: Tab by singleAssign()
    private var runStatsTab: Tab by singleAssign()
    private var hikeStatsTab: Tab by singleAssign()
    private var inlineSkateStatsTab: Tab by singleAssign()

    init {
        with(root) {
            left {
                vbox {
                    prefWidth = 180.0
                    style {
                        spacing = 5.px
                        padding = box(5.px)
                    }
                    textfield("${athlete?.firstname} ${athlete?.lastname}") {
                        isEditable = false
                        maxWidth = Double.MAX_VALUE
                    }
                    if (MyStravaStatsApp.myStravaStatsParameters.year == null) {
                        combobox(property = selectedYear, values = (LocalDate.now().year downTo 2010).toList()) {
                            selectionModel.selectedItemProperty().onChange {
                                updateTabs()
                            }
                            maxWidth = Double.MAX_VALUE
                        }
                    } else {
                        textfield(value = MyStravaStatsApp.myStravaStatsParameters.year.toString()) {
                            isEditable = false
                            maxWidth = Double.MAX_VALUE
                        }
                    }
                    button("Generate CSV") {
                        action {
                            mainController.generateCSV()
                        }
                        maxWidth = Double.MAX_VALUE
                    }
                    button("Generate Charts") {
                        action {
                            mainController.generateCharts()
                        }
                        maxWidth = Double.MAX_VALUE
                    }
                }
            }
            center {
                tabpane {
                    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                    tab("Global statistics") { globalStatsTab = this }
                    tab("Ride statistics") { sportRideStatsTab = this }
                    tab("Commute ride statistics") { commuteRideStatsTab = this }
                    tab("Run statistics") { runStatsTab = this }
                    tab("Hike ride statistics") { hikeStatsTab = this }
                    tab("InlineSkate statistics") { inlineSkateStatsTab = this }
                }
            }
        }
        updateTabs()
    }

    private fun updateTabs() {
        val statisticsToDisplay = mainController.getStatisticsToDisplay(selectedYear.value)

        globalStatsTab.content = vbox {
            tableview(statisticsToDisplay.globalStatistics) {
                readonlyColumn("Statistic", StatisticDisplay::label)
                readonlyColumn("Value", StatisticDisplay::value)
            }
            drawer {
                item("Distance by months", expanded = true) {
                    barchart("Distance by months for ${selectedYear.value} (km)", CategoryAxis(), NumberAxis()) {
                        series(Run, mainController.buildDistanceByMonthsSeries(Run, selectedYear.value))
                        series(Ride, mainController.buildDistanceByMonthsSeries(Ride, selectedYear.value))
                        series(InlineSkate, mainController.buildDistanceByMonthsSeries(InlineSkate, selectedYear.value))
                        series(Hike, mainController.buildDistanceByMonthsSeries(Hike, selectedYear.value))
                        verticalGridLinesVisible = false
                    }
                }
            }
        }
        sportRideStatsTab.content = vbox {
            tableview(statisticsToDisplay.sportRideStatistics) {
                readonlyColumn("Statistic", StatisticDisplay::label)
                readonlyColumn("Value", StatisticDisplay::value)
                readonlyColumn("Activity", StatisticDisplay::activity)
            }
            drawer {
                item("Distance/months", expanded = true) {
                    barchart("Distance by months for ${selectedYear.value} (km)", CategoryAxis(), NumberAxis()) {
                        series(Ride, mainController.buildDistanceByMonthsSeries(Ride, selectedYear.value))
                        verticalGridLinesVisible = false
                        isLegendVisible = false
                    }
                }
                item("Distance/days") {
                    barchart("Distance by days for ${selectedYear.value} (km)", CategoryAxis(), NumberAxis()) {
                        series(Ride, mainController.buildDistanceByDaysSeries(Ride, selectedYear.value))
                        verticalGridLinesVisible = false
                        isLegendVisible = false
                    }
                }
                item("Eddington number") {
                    eddingtonNumberChart(mainController.getActiveDaysByActivityType(Ride))
                }
            }
            commuteRideStatsTab.content =
                tableview(statisticsToDisplay.commuteRideStatistics) {
                    readonlyColumn("Statistic", StatisticDisplay::label)
                    readonlyColumn("Value", StatisticDisplay::value)
                    readonlyColumn("Activity", StatisticDisplay::activity)
                }
            runStatsTab.content = vbox {
                tableview(statisticsToDisplay.runStatistics) {
                    readonlyColumn("Statistic", StatisticDisplay::label)
                    readonlyColumn("Value", StatisticDisplay::value)
                    readonlyColumn("Activity", StatisticDisplay::activity)
                }
                drawer {
                    item("Distance by months", expanded = true) {
                        barchart("Distance by months for ${selectedYear.value} (km)", CategoryAxis(), NumberAxis()) {
                            series(Run, mainController.buildDistanceByMonthsSeries(Run, selectedYear.value))
                            verticalGridLinesVisible = false
                            isLegendVisible = false
                        }
                    }
                    item("Distance by days") {
                        barchart("Distance by days for ${selectedYear.value} (km)", CategoryAxis(), NumberAxis()) {
                            series(Run, mainController.buildDistanceByDaysSeries(Run, selectedYear.value))
                            verticalGridLinesVisible = false
                            isLegendVisible = false
                        }
                    }
                    item("Eddington number") {
                        eddingtonNumberChart(mainController.getActiveDaysByActivityType(Run))
                    }
                }
            }
            hikeStatsTab.content = vbox {
                tableview(statisticsToDisplay.hikeStatics) {
                    readonlyColumn("Statistic", StatisticDisplay::label)
                    readonlyColumn("Value", StatisticDisplay::value)
                    readonlyColumn("Activity", StatisticDisplay::activity)
                }
                drawer {
                    item("Distance by months", expanded = true) {
                        barchart("Distance by months for ${selectedYear.value} (km)", CategoryAxis(), NumberAxis()) {
                            series(Hike, mainController.buildDistanceByMonthsSeries(Hike, selectedYear.value))
                            verticalGridLinesVisible = false
                            isLegendVisible = false
                        }
                    }
                    item("Distance by days") {
                        barchart("Distance by days for ${selectedYear.value} (km)", CategoryAxis(), NumberAxis()) {
                            series(Hike, mainController.buildDistanceByDaysSeries(Hike, selectedYear.value))
                            verticalGridLinesVisible = false
                            isLegendVisible = false
                        }
                    }
                    item("Eddington number") {
                        eddingtonNumberChart(mainController.getActiveDaysByActivityType(Hike))
                    }
                }
            }
            inlineSkateStatsTab.content = vbox {
                tableview(statisticsToDisplay.inlineSkateStatistics) {
                    readonlyColumn("Statistic", StatisticDisplay::label)
                    readonlyColumn("Value", StatisticDisplay::value)
                    readonlyColumn("Activity", StatisticDisplay::activity)
                }
                drawer {
                    item("Distance by months", expanded = true) {
                        barchart("Distance by months for ${selectedYear.value} (km)", CategoryAxis(), NumberAxis()) {
                            series(
                                InlineSkate,
                                mainController.buildDistanceByMonthsSeries(InlineSkate, selectedYear.value)
                            )
                            verticalGridLinesVisible = false
                            isLegendVisible = false
                        }
                    }
                    item("Distance by days") {
                        barchart("Distance by days for ${selectedYear.value} (km)", CategoryAxis(), NumberAxis()) {
                            series(
                                InlineSkate,
                                mainController.buildDistanceByDaysSeries(InlineSkate, selectedYear.value)
                            )
                            verticalGridLinesVisible = false
                            isLegendVisible = false
                        }
                    }
                    item("Eddington number") {
                        eddingtonNumberChart(mainController.getActiveDaysByActivityType(InlineSkate))
                    }
                }
            }
        }
    }

    private fun multipleAxesLineChart(): MultipleAxesLineChart {

        val activitiesByYear = mainController.getActivitiesByYear()
        val allSeries = mutableListOf<XYChart.Series<String, Number>>()
        for (year in 2010..LocalDate.now().year) {
            val activities = if (activitiesByYear[year.toString()] != null) {
                activitiesByYear[year.toString()]?.filter { activity -> activity.type == Ride }!!
            } else {
                continue
            }
            val activitiesByDay = ActivityHelper.groupActivitiesByDay(activities, year)
            val cumulativeDistance = ActivityHelper.cumulativeDistance(activitiesByDay)
            val data = cumulativeDistance.entries.map { entry ->
                XYChart.Data<String, Number>(entry.key, entry.value)
            }.toObservable()
            allSeries.add(XYChart.Series(data))
        }

        val yAxis = NumberAxis().apply {
            title = "Distance (km)"
            side = Side.LEFT

        }
        val baseChart = LineChart(CategoryAxis(), yAxis)
        baseChart.data.add(allSeries.removeFirst())
        val multipleAxesLineChart = MultipleAxesLineChart(baseChart)
        allSeries.map { series ->
            multipleAxesLineChart.addSeries(series)
        }

        return multipleAxesLineChart
    }
}