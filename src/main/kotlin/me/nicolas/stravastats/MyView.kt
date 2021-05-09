package me.nicolas.stravastats

import com.beust.jcommander.JCommander
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Tab
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.business.Statistic
import me.nicolas.stravastats.core.StravaService
import me.nicolas.stravastats.core.StatisticsService
import me.nicolas.stravastats.strava.StravaApi
import tornadofx.*
import java.time.LocalDate

class MainView : View("MyStravaStats") {
    override val root = BorderPane()

    private val mainController: MainController by inject()

    private val years = FXCollections.observableArrayList((LocalDate.now().year downTo 2010).toList())
    private var selectedYear = SimpleIntegerProperty(LocalDate.now().year)

    private val athlete: Athlete? = mainController.getLoggedInAthlete()

    var globalStatsTab: Tab by singleAssign()
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
                        fieldset("Athlete") {
                            label("${athlete?.firstname} ${athlete?.lastname}")
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
                    tab("Global") {
                        globalStatsTab = this
                        tableview(mainController.getDisplayStatistics("Global")) {
                            readonlyColumn("name", StatisticDisplay::label)
                            readonlyColumn("", StatisticDisplay::value)
                        }
                    }
                    tab("Ride stats") {
                        rideStatsTab = this
                        tableview(mainController.getDisplayStatistics("Ride")) {
                            readonlyColumn("name", StatisticDisplay::label)
                            readonlyColumn("", StatisticDisplay::value)
                        }
                    }
                    tab("Commute ride stats") {
                        commuteRideStatsTab = this
                        tableview(mainController.getDisplayStatistics("Commute ride")) {
                            readonlyColumn("name", StatisticDisplay::label)
                            readonlyColumn("", StatisticDisplay::value)
                        }
                    }
                    tab("Run stats") {
                        runsStatsTab = this
                        tableview(mainController.getDisplayStatistics("Run")) {
                            readonlyColumn("name", StatisticDisplay::label)
                            readonlyColumn("", StatisticDisplay::value)
                        }
                    }
                    tab("Hike ride stats") {
                        hikesStatsTab = this
                        tableview(mainController.getDisplayStatistics("Hike")) {
                            readonlyColumn("name", StatisticDisplay::label)
                            readonlyColumn("", StatisticDisplay::value)
                        }
                    }
                    tab("InlineSkate stats") {
                        inlineSkateStatsTab = this
                        tableview(mainController.getDisplayStatistics("InlineSkate")) {
                            readonlyColumn("name", StatisticDisplay::label)
                            readonlyColumn("", StatisticDisplay::value)
                        }
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

data class StatisticDisplay(val label: String, val value: String)

class MainController : Controller() {

    private val myStravaStatsProperties = loadPropertiesFromFile()

    private val stravaService = StravaService(StravaApi(myStravaStatsProperties))

    private val statsService = StatisticsService()

    private val activities = stravaService.loadActivities(
        MyStravaStatsApp.myStravaStatsParameters.clientId,
        MyStravaStatsApp.myStravaStatsParameters.clientSecret,
        MyStravaStatsApp.myStravaStatsParameters.year
    )

    private val stravaStatistics = statsService.computeStatistics(activities)


    fun getLoggedInAthlete(): Athlete? {
        return stravaService.getLoggedInAthlete(
            MyStravaStatsApp.myStravaStatsParameters.clientId,
            MyStravaStatsApp.myStravaStatsParameters.clientSecret
        )
    }

    fun getDisplayStatistics(type: String): ObservableList<StatisticDisplay> {
        val statistics = when (type) {
            "Run" -> stravaStatistics.runsStats
            "Ride" -> stravaStatistics.sportRideStats
            "Commute ride" -> stravaStatistics.commuteRideStats
            "InlineSkate" -> stravaStatistics.inlineSkateStats
            "Global" -> stravaStatistics.globalStatistic
            "Hike" -> stravaStatistics.hikesStats
            else -> emptyList()
        }

        return getDisplayStatistics(statistics)
    }

    fun getDisplayStatistics(statistics: List<Statistic>): ObservableList<StatisticDisplay> {
        val activityStatistics = statistics.map { statistic ->
            StatisticDisplay(statistic.name, statistic.display())
        }
        return FXCollections.observableArrayList(activityStatistics)
    }

    /**
     * Load properties from application.yml
     */
    private fun loadPropertiesFromFile(): MyStravaStatsProperties {

        val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
        mapper.registerModule(KotlinModule()) // Enable Kotlin support

        val inputStream = javaClass.getResourceAsStream("/application.yml")
        return mapper.readValue(inputStream, MyStravaStatsProperties::class.java)
    }
}

class MyStravaStatsApp : App(MainView::class) {

    companion object {
        val myStravaStatsParameters = MyStravaStatsParameters()
    }

    override fun init() {
        JCommander.newBuilder()
            .addObject(myStravaStatsParameters)
            .programName("My Strava Stats")
            .build().parse(*parameters.raw.toTypedArray())
    }
}

fun main(args: Array<String>) {
    launch<MyStravaStatsApp>(args)
}