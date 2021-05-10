package me.nicolas.stravastats.ihm

import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import me.nicolas.stravastats.MyStravaStatsApp
import me.nicolas.stravastats.business.*
import tornadofx.*
import java.time.LocalDate


class MainView : View("MyStravaStats") {
    override val root = BorderPane()

    private val mainController: MainController by inject()

    private val years = FXCollections.observableArrayList((LocalDate.now().year downTo 2010).toList())
    private var selectedYear = SimpleIntegerProperty(LocalDate.now().year)

    private val athlete: Athlete? = mainController.getLoggedInAthlete()

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
                        combobox(property = selectedYear, values = years) {
                            selectionModel.selectedItemProperty().onChange {
                                updateStatistics()
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
        updateStatistics()
    }

    private fun updateStatistics() {
        val statisticsToDisplay = mainController.getStatisticsToDisplay(selectedYear.value)

        globalStatsTab.content = vbox {
            tableview(statisticsToDisplay.globalStatistics) {
                readonlyColumn("Statistic", StatisticDisplay::label)
                readonlyColumn("Value", StatisticDisplay::value)
            }
            drawer {
                item("Distance by months", expanded = true) {
                    barchart("Distance by months for ${selectedYear.value} (km/h)", CategoryAxis(), NumberAxis()) {
                        series(Run, mainController.buildDistanceByMonthsSeries(Run, selectedYear.value))
                        series(Ride, mainController.buildDistanceByMonthsSeries(Ride, selectedYear.value))
                        series(InlineSkate, mainController.buildDistanceByMonthsSeries(InlineSkate, selectedYear.value))
                        series(Hike, mainController.buildDistanceByMonthsSeries(Hike, selectedYear.value))
                        verticalGridLinesVisible = false
                    }
                }
                item("Distance by months") {
                    stackedbarchart(
                        "Distance by months for ${selectedYear.value} (km/h)", CategoryAxis(), NumberAxis()
                    ) {
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
                item("Distance by months", expanded = true) {
                    barchart("Distance by months for ${selectedYear.value} (km/h)", CategoryAxis(), NumberAxis()) {
                        series(Ride, mainController.buildDistanceByMonthsSeries(Ride, selectedYear.value))
                        verticalGridLinesVisible = false
                    }
                }
                item("Distance by days") {
                    barchart("Distance by days for ${selectedYear.value} (km/h)", CategoryAxis(), NumberAxis()) {
                        series(Ride, mainController.buildDistanceByDaysSeries(Ride, selectedYear.value))
                        verticalGridLinesVisible = false
                    }
                }
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
                    barchart("Distance by months for ${selectedYear.value} (km/h)", CategoryAxis(), NumberAxis()) {
                        series(Run, mainController.buildDistanceByMonthsSeries(Run, selectedYear.value))
                        verticalGridLinesVisible = false
                    }
                }
                item("Distance by days") {
                    barchart("Distance by days for ${selectedYear.value} (km/h)", CategoryAxis(), NumberAxis()) {
                        series(Run, mainController.buildDistanceByDaysSeries(Run, selectedYear.value))
                        verticalGridLinesVisible = false
                    }
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
                    barchart("Distance by months for ${selectedYear.value} (km/h)", CategoryAxis(), NumberAxis()) {
                        series(Hike, mainController.buildDistanceByMonthsSeries(Hike, selectedYear.value))
                        verticalGridLinesVisible = false
                    }
                }
                item("Distance by days") {
                    barchart("Distance by days for ${selectedYear.value} (km/h)", CategoryAxis(), NumberAxis()) {
                        series(Hike, mainController.buildDistanceByDaysSeries(Hike, selectedYear.value))
                        verticalGridLinesVisible = false
                    }
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
                    barchart("Distance by months for ${selectedYear.value} (km/h)", CategoryAxis(), NumberAxis()) {
                        series(InlineSkate, mainController.buildDistanceByMonthsSeries(InlineSkate, selectedYear.value))
                        verticalGridLinesVisible = false
                    }
                }
                item("Distance by days") {
                    barchart("Distance by days for ${selectedYear.value} (km/h)", CategoryAxis(), NumberAxis()) {
                        series(InlineSkate, mainController.buildDistanceByDaysSeries(InlineSkate, selectedYear.value))
                        verticalGridLinesVisible = false
                    }
                }
            }
        }
    }
}
