package me.nicolas.stravastats.ihm

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.Pane
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.business.Ride
import me.nicolas.stravastats.service.ActivityHelper
import tornadofx.*
import java.time.LocalDate


class MainView(
    clientId: String,
    athlete: Athlete?,
    activities: ObservableList<Activity>
) : View("MyStravaStats") {

    override val root = borderpane {
        setPrefSize(1200.0, 800.0)
    }

    private val mainController: MainController = MainController(clientId, activities)

    private var selectedYear = SimpleIntegerProperty(LocalDate.now().year)
    private var selectedActivity = SimpleStringProperty("Ride")

    private var statisticsTab: Tab by singleAssign()
    private var activitiesTab: Tab by singleAssign()
    private var chartsTab: Tab by singleAssign()
    private var overYearsTab: Tab by singleAssign()

    init {
        FX.primaryStage.isResizable = true

        with(root) {
            left {
                vbox {
                    prefWidth = 180.0
                    style {
                        spacing = 5.px
                        padding = box(5.px)
                    }
                    textfield("${athlete?.firstname ?: ""} ${athlete?.lastname ?: ""}") {
                        isEditable = false
                        maxWidth = Double.MAX_VALUE
                    }
                    combobox(property = selectedYear, values = (LocalDate.now().year downTo 2010).toList()) {
                        selectionModel.selectedItemProperty().onChange {
                            updateTabs()
                        }
                        maxWidth = Double.MAX_VALUE
                    }
                    combobox(
                        property = selectedActivity,
                        values = listOf("Ride", "Commute", "Run", "InlineSkate", "Hike")
                    ) {
                        selectionModel.selectedItemProperty().onChange {
                            updateTabs()
                        }
                        maxWidth = Double.MAX_VALUE
                    }
                    button("Generate CSV") {
                        action {
                            mainController.generateCSV(selectedYear.value)
                        }
                        maxWidth = Double.MAX_VALUE
                    }
                    button("Generate Charts") {
                        action {
                            mainController.generateCharts(selectedYear.value)
                        }
                        maxWidth = Double.MAX_VALUE
                    }
                }
            }
            center {
                tabpane {
                    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                    tab("Activities") { activitiesTab = this }
                    tab("Statistics") { statisticsTab = this }
                    tab("Charts") { chartsTab = this }
                    tab("Over years") { overYearsTab = this }
                }
            }
        }
        updateTabs()
    }

    private fun updateTabs() {
        val statisticsToDisplay = mainController.getStatisticsToDisplay(selectedActivity.value, selectedYear.value)
        val activities = mainController.getActivitiesToDisplay(selectedActivity.value, selectedYear.value)

        activitiesTab.content = tableview(activities) {
            readonlyColumn("Activity", ActivityDisplay::activity)
            readonlyColumn("Distance", ActivityDisplay::distance).setComparator { d1, d2 ->
                d1.replace(",", ".").toDouble().compareTo(d2.replace(",", ".").toDouble())
            }
            readonlyColumn("Date", ActivityDisplay::date)
            resizeColumnsToFitContent()
        }

        statisticsTab.content = tableview(statisticsToDisplay) {
            readonlyColumn("Statistic", StatisticDisplay::label)
            readonlyColumn("Value", StatisticDisplay::value)
            readonlyColumn("Activity", StatisticDisplay::activity)
            resizeColumnsToFitContent()
        }

        chartsTab.content = drawer {
            item("Distance by months", expanded = true) {
                barchart("Distance by months for ${selectedYear.value} (km)", CategoryAxis(), NumberAxis()) {
                    series(Ride, mainController.buildDistanceByMonthsSeries(selectedActivity.value, selectedYear.value))
                    verticalGridLinesVisible = false
                    isLegendVisible = false
                }
            }
            item("Distance by days") {
                barchart("Distance by days for ${selectedYear.value} (km)", CategoryAxis(), NumberAxis()) {
                    series(Ride, mainController.buildDistanceByDaysSeries(selectedActivity.value, selectedYear.value))
                    verticalGridLinesVisible = false
                    isLegendVisible = false
                }
            }
            item("Eddington number") {
                eddingtonNumberChart(
                    mainController.getActiveDaysByActivityTypeByYear(
                        selectedActivity.value,
                        selectedYear.value
                    )
                )
            }
        }
        overYearsTab.content = drawer {

            item("${selectedActivity.value} distance by years", expanded = true) {
                val multipleAxesLineChart = distanceByYears(selectedActivity.value)
                multipleAxesLineChart.attachTo(this)
            }
            item("${selectedActivity.value} Eddington number") {
                eddingtonNumberChart(mainController.getActiveDaysByActivityType(selectedActivity.value))
            }
        }
    }

    private fun distanceByYears(activityType: String): Pane {

        val activitiesByYear = mainController.getActivitiesByYear(activityType)
        val allSeries = mutableListOf<XYChart.Series<String, Number>>()
        for (year in 2010..LocalDate.now().year) {
            val activities = if (activitiesByYear[year.toString()] != null) {
                activitiesByYear[year.toString()]!!
            } else {
                continue
            }
            val activitiesByDay = ActivityHelper.groupActivitiesByDay(activities, year)
            val cumulativeDistance = ActivityHelper.cumulativeDistance(activitiesByDay)
            val data = cumulativeDistance.entries.map { entry ->
                XYChart.Data<String, Number>(entry.key, entry.value)
            }.toObservable()
            allSeries.add(XYChart.Series(year.toString(), data))
        }
        return if (allSeries.isEmpty()) {
            Pane()
        } else {
            return MultipleLineChart("$activityType distance (km) by years", allSeries)
        }
    }
}