package me.nicolas.stravastats.ihm

import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import me.nicolas.stravastats.MyStravaStatsApp
import me.nicolas.stravastats.business.Athlete
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
            top {
                form {
                    fieldset {
                        style {
                            spacing = 5.px
                            padding = box(20.px)
                        }
                        field("Athlete") {
                            textfield("${athlete?.firstname} ${athlete?.lastname}") {
                                isEditable = false
                            }
                        }
                        field("Year") {
                            if (MyStravaStatsApp.myStravaStatsParameters.year == null) {
                                combobox(property = selectedYear, values = years) {
                                    selectionModel.selectedItemProperty().onChange {
                                        updateStatistics()
                                    }
                                }
                            } else {
                                textfield(value = MyStravaStatsApp.myStravaStatsParameters.year.toString()) {
                                    isEditable = false
                                }
                            }
                        }
                    }
                }
            }
            center {
                tabpane {
                    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                    tab("Global") { globalStatsTab = this }
                    tab("Ride stats") { sportRideStatsTab = this }
                    tab("Commute ride stats") { commuteRideStatsTab = this }
                    tab("Run stats") { runStatsTab = this }
                    tab("Hike ride stats") { hikeStatsTab = this }
                    tab("InlineSkate stats") { inlineSkateStatsTab = this }
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
        }
        sportRideStatsTab.content = tableview(statisticsToDisplay.sportRideStatistics) {
            readonlyColumn("Statistic", StatisticDisplay::label)
            readonlyColumn("Value", StatisticDisplay::value)
            readonlyColumn("Activity", StatisticDisplay::activity)
        }
        commuteRideStatsTab.content = tableview(statisticsToDisplay.commuteRideStatistics) {
            readonlyColumn("Statistic", StatisticDisplay::label)
            readonlyColumn("Value", StatisticDisplay::value)
            readonlyColumn("Activity", StatisticDisplay::activity)
        }
        runStatsTab.content = tableview(statisticsToDisplay.runStatistics) {
            readonlyColumn("Statistic", StatisticDisplay::label)
            readonlyColumn("Value", StatisticDisplay::value)
            readonlyColumn("Activity", StatisticDisplay::activity)
        }
        hikeStatsTab.content = tableview(statisticsToDisplay.hikeStatics) {
            readonlyColumn("Statistic", StatisticDisplay::label)
            readonlyColumn("Value", StatisticDisplay::value)
            readonlyColumn("Activity", StatisticDisplay::activity)
        }
        inlineSkateStatsTab.content = tableview(statisticsToDisplay.inlineSkateStatistics) {
            readonlyColumn("Statistic", StatisticDisplay::label)
            readonlyColumn("Value", StatisticDisplay::value)
            readonlyColumn("Activity", StatisticDisplay::activity)
        }
    }
}
