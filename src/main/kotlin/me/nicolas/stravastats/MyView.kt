package me.nicolas.stravastats

import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Tab
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text
import tornadofx.*
import java.time.LocalDate

class MainView : View("MyStravaStats") {
    override val root = BorderPane()

    private val years: ObservableList<Int> =
        FXCollections.observableArrayList((LocalDate.now().year downTo 2010).toList())
    private var selectedYear = SimpleIntegerProperty(LocalDate.now().year)

    private var clientIdTextField: TextField by singleAssign()

    var rideStatsTab: Tab by singleAssign()
    var commuteRideStatsTab: Tab by singleAssign()
    var runsStatsTab: Tab by singleAssign()
    var hikesStatsTab: Tab by singleAssign()
    var inlineSkateStatsTab: Tab by singleAssign()

    init {
        with(root) {
            top {
                form {
                    hbox {
                        fieldset("Strava") {
                            label("Client Id")
                            textfield("Client Id") {
                                clientIdTextField = this
                            }
                        }
                        fieldset("Year") {
                            combobox(property = selectedYear, values = years) {
                                selectionModel.selectedItemProperty().onChange {
                                    updateStatistics()
                                }
                            }
                        }
                    }
                }
            }
            center {
                tabpane {
                    tab("Ride stats") {
                        rideStatsTab = this
                        text("Ride stats for ${selectedYear.value}")
                    }
                    tab("Commute ride stats") {
                        commuteRideStatsTab = this

                        text("Commute ride stats for ${selectedYear.value}")
                    }
                    tab("Run stats") {
                        runsStatsTab = this

                        text("Run stats for ${selectedYear.value}")
                    }
                    tab("Hike ride stats") {
                        hikesStatsTab = this

                        text("Hike stats for ${selectedYear.value}")
                    }
                    tab("InlineSkate stats") {
                        inlineSkateStatsTab = this

                        text("InlineSkate stats for ${selectedYear.value}")
                    }
                }
            }
        }
    }

    private fun updateStatistics() {
        rideStatsTab.content = Text("Ride for ${selectedYear.value}")
        commuteRideStatsTab.content = Text("Run for ${selectedYear.value}")
        runsStatsTab.content = Text("Ride for ${selectedYear.value}")
        hikesStatsTab.content = Text("Run for ${selectedYear.value}")
        inlineSkateStatsTab.content = Text("Ride for ${selectedYear.value}")
    }
}

class MyApp : App(MainView::class)

fun main(args: Array<String>) {
    launch<MyApp>(args)
}