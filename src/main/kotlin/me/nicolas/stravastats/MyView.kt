package me.nicolas.stravastats

import com.beust.jcommander.JCommander
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text
import javafx.stage.Stage
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.core.statistics.Statistic
import me.nicolas.stravastats.core.StravaService
import me.nicolas.stravastats.core.StatisticsService
import me.nicolas.stravastats.core.statistics.ActivityStatistic
import me.nicolas.stravastats.strava.StravaApi
import tornadofx.*
import java.time.LocalDate

class MainView : View("MyStravaStats") {
    override val root = BorderPane()

    private val mainController: MainController by inject()

    private val years = FXCollections.observableArrayList((LocalDate.now().year downTo 2010).toList())
    private var selectedYear = SimpleIntegerProperty(LocalDate.now().year)

    private val athlete: Athlete? = mainController.getLoggedInAthlete()

    private var globalStatsTab: Tab by singleAssign()
    private var rideStatsTab: Tab by singleAssign()
    private var commuteRideStatsTab: Tab by singleAssign()
    private var runStatsTab: Tab by singleAssign()
    private var hikeStatsTab: Tab by singleAssign()
    private var inlineSkateStatsTab: Tab by singleAssign()

    init {
        with(root) {
            top {
                form {
                    hbox {
                        style {
                            spacing = 5.px
                            padding = box(20.px)
                        }
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
                    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                    tab("Global") { globalStatsTab = this }
                    tab("Ride stats") { rideStatsTab = this }
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
        globalStatsTab.content = tableview(mainController.getStatisticsDisplay("Global")) {
            readonlyColumn("name", StatisticDisplay::label)
            readonlyColumn("value", StatisticDisplay::value)
        }
        rideStatsTab.content =tableview(mainController.getStatisticsDisplay("Ride")) {
            readonlyColumn("name", StatisticDisplay::label)
            readonlyColumn("value", StatisticDisplay::value)
            readonlyColumn("activity", StatisticDisplay::activity)
        }
        commuteRideStatsTab.content = tableview(mainController.getStatisticsDisplay("Commute ride")) {
            readonlyColumn("name", StatisticDisplay::label)
            readonlyColumn("value", StatisticDisplay::value)
            readonlyColumn("activity", StatisticDisplay::activity)
        }
        runStatsTab.content = tableview(mainController.getStatisticsDisplay("Run")) {
            readonlyColumn("name", StatisticDisplay::label)
            readonlyColumn("value", StatisticDisplay::value)
            readonlyColumn("activity", StatisticDisplay::activity)
        }
        hikeStatsTab.content = tableview(mainController.getStatisticsDisplay("Hike")) {
            readonlyColumn("name", StatisticDisplay::label)
            readonlyColumn("value", StatisticDisplay::value)
            readonlyColumn("activity", StatisticDisplay::activity)
        }
        inlineSkateStatsTab.content = tableview(mainController.getStatisticsDisplay("InlineSkate")) {
            readonlyColumn("name", StatisticDisplay::label)
            readonlyColumn("", StatisticDisplay::value)
            readonlyColumn("activity", StatisticDisplay::activity)
        }
    }
}

data class StatisticDisplay(val label: String, val value: String, val activity: String)

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

    fun getStatisticsDisplay(type: String): ObservableList<StatisticDisplay> {
        val statistics = when (type) {
            "Run" -> stravaStatistics.runsStats
            "Ride" -> stravaStatistics.sportRideStats
            "Commute ride" -> stravaStatistics.commuteRideStats
            "InlineSkate" -> stravaStatistics.inlineSkateStats
            "Global" -> stravaStatistics.globalStatistic
            "Hike" -> stravaStatistics.hikesStats
            else -> emptyList()
        }

        return getStatisticsDisplay(statistics)
    }

    private fun getStatisticsDisplay(statistics: List<Statistic>): ObservableList<StatisticDisplay> {
        val activityStatistics = statistics.map { statistic ->
            when (statistic) {
                is ActivityStatistic -> {
                    StatisticDisplay(
                        statistic.name,
                        statistic.value,
                        if(statistic.activity != null) statistic.activity.toString() else ""
                    )
                }
                else -> StatisticDisplay(statistic.name, statistic.toString(), "")
            }
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

    override fun start(stage: Stage) {
        super.start(stage)
        stage.width = 1024.0
        stage.height = 768.0
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