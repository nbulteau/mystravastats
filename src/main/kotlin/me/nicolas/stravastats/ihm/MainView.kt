package me.nicolas.stravastats.ihm

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.util.Callback
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.business.Ride
import me.nicolas.stravastats.business.Run
import me.nicolas.stravastats.business.badges.FamousClimbBadge
import me.nicolas.stravastats.ihm.chart.MultipleLineChart
import me.nicolas.stravastats.ihm.chart.eddingtonNumberChart
import me.nicolas.stravastats.service.ActivityHelper
import me.nicolas.stravastats.service.formatSeconds
import me.nicolas.stravastats.service.formatSpeed
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
    private var selectedActivity = selectFirstDisplayActivity(activities)

    private var statisticsTab: Tab by singleAssign()
    private var activitiesTab: Tab by singleAssign()
    private var chartsTab: Tab by singleAssign()
    private var badgesTab: Tab by singleAssign()
    private var overYearsTab: Tab by singleAssign()

    // To manage year selection in MultipleLineChart
    private lateinit var multipleAxesLineChartPane: Pane
    private lateinit var borderPane: BorderPane

    private val detailsWindow: AnchorPane
    private val detailsPopup: DetailsPopup

    private lateinit var activeYearsSet: MutableSet<String>

    init {
        FX.primaryStage.isResizable = true

        with(root) {
            top {
                borderpane {
                    left {
                        hbox {
                            style {
                                spacing = 5.px
                                padding = box(5.px)

                            }
                            alignment = Pos.CENTER_LEFT

                            textfield("${athlete?.firstname ?: ""} ${athlete?.lastname ?: clientId}") {
                                isEditable = false
                                prefWidth = 150.0
                            }
                            combobox(property = selectedYear, values = (LocalDate.now().year downTo 2010).toList()) {
                                selectionModel.selectedItemProperty().onChange {
                                    updateTabs()
                                }
                                prefWidth = 80.0
                            }
                        }
                    }
                    center {
                        hbox {
                            style {
                                spacing = 5.px
                                padding = box(5.px)

                            }
                            alignment = Pos.CENTER_LEFT

                            button {
                                imageview("images/buttons/ride.png")
                                action {
                                    selectedActivity = SimpleStringProperty("Ride")
                                    updateTabs()
                                }
                            }
                            button {
                                imageview("images/buttons/commute.png")
                                action {
                                    selectedActivity = SimpleStringProperty("Commute")
                                    updateTabs()
                                }
                            }
                            button {
                                requestFocus()
                                imageview("images/buttons/run.png")
                                action {
                                    selectedActivity = SimpleStringProperty("Run")
                                    updateTabs()
                                }
                            }
                            button {
                                imageview("images/buttons/inlineskate.png")
                                action {
                                    selectedActivity = SimpleStringProperty("InlineSkate")
                                    updateTabs()
                                }
                            }
                            button {
                                imageview("images/buttons/hike.png")
                                action {
                                    selectedActivity = SimpleStringProperty("Hike")
                                    updateTabs()
                                }
                            }
                            button {
                                imageview("images/buttons/alpineski.png")
                                action {
                                    selectedActivity = SimpleStringProperty("AlpineSki")
                                    updateTabs()
                                }
                            }
                        }
                    }
                    right {
                        hbox {
                            style {
                                spacing = 5.px
                                padding = box(5.px)

                            }
                            alignment = Pos.CENTER_RIGHT

                            button {
                                imageview("images/buttons/csv.png")
                                action {
                                    mainController.generateCSV(selectedYear.value)
                                }
                            }
                            button {
                                imageview("images/buttons/charts.png")
                                action {
                                    mainController.generateCharts(selectedYear.value)
                                }
                            }
                        }
                    }
                }
            }
            center {
                vbox {
                    tabpane {
                        tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                        tab("Activities") { activitiesTab = this }
                        tab("Statistics") { statisticsTab = this }
                        tab("Charts") { chartsTab = this }
                        tab("Badges") { badgesTab = this }
                        tab("Over years") { overYearsTab = this }
                    }
                    pane {

                    }
                }
            }
        }

        detailsWindow = AnchorPane()
        detailsPopup = DetailsPopup()
        detailsWindow.children.add(detailsPopup)
        detailsWindow.prefHeightProperty().bind(root.heightProperty())
        detailsWindow.prefWidthProperty().bind(root.widthProperty())
        detailsWindow.isMouseTransparent = true
        badgesTab.tabPane.getChildList()?.add(detailsWindow)

        updateTabs()
    }

    private fun selectFirstDisplayActivity(activities: ObservableList<Activity>): SimpleStringProperty {
        val nbRunActivities = activities.count { activity -> activity.type == Run }
        val nbRideActivities = activities.count { activity -> activity.type == Ride }

        return if (nbRunActivities > nbRideActivities) {
            SimpleStringProperty(Run)
        } else {
            SimpleStringProperty(Ride)
        }
    }

    private fun updateTabs() {
        activeYearsSet = (LocalDate.now().year downTo 2010).map { "$it" }.toMutableSet()
        val statisticsToDisplay = mainController.getStatisticsToDisplay(selectedActivity.value, selectedYear.value)
        val activitiesToDisplay = mainController.getActivitiesToDisplay(selectedActivity.value, selectedYear.value)
        val generalBadgesSetToDisplay = mainController.getGeneralBadgesSetToDisplay(selectedActivity.value)

        activitiesTab.content = tableview(activitiesToDisplay) {
            readonlyColumn("Activity", ActivityDisplay::name)
            readonlyColumn("Distance", ActivityDisplay::distance).cellFactory = formatDistance()
            readonlyColumn("Elapsed time", ActivityDisplay::elapsedTime).cellFactory = formatSeconds()
            readonlyColumn("Total elevation gain", ActivityDisplay::totalElevationGain).cellFactory = formatElevation()
            readonlyColumn("Average speed", ActivityDisplay::averageSpeed).cellFactory =
                formatSpeed(selectedActivity.value)
            readonlyColumn("Best speed for 1000 m", ActivityDisplay::bestTimeForDistanceFor1000m)
            if (selectedActivity.value != "AlpineSki") {
                readonlyColumn("Max slope for 250 m", ActivityDisplay::BestElevationForDistanceFor250m)
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
            item("Distance by weeks") {
                barchart("Distance by weeks for ${selectedYear.value} (km)", CategoryAxis(), NumberAxis()) {
                    series(Ride, mainController.buildDistanceByWeeksSeries(selectedActivity.value, selectedYear.value))
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
        badgesTab.content = if (selectedActivity.value == "Ride") {
            val famousClimbBadgesSetToDisplay = mainController.getFamousClimbBadgesSetToDisplay(selectedActivity.value)

            drawer {
                item("General", expanded = true) {
                    this.add(buildGeneralBadgesSetScrollpane(generalBadgesSetToDisplay))
                }
                item("Famous climb", expanded = false) {
                    this.add(buildFamousClimbBadgesSetScrollpane(famousClimbBadgesSetToDisplay))
                }
            }
        } else {
            buildGeneralBadgesSetScrollpane(generalBadgesSetToDisplay)
        }

        overYearsTab.content = drawer {
            item("${selectedActivity.value} distance per year cumulative", expanded = true) {
                borderpane {
                    top {
                        hbox {
                            style {
                                spacing = 5.px
                                padding = box(5.px)
                            }
                            alignment = Pos.CENTER
                            for (year in 2010..LocalDate.now().year) {
                                val activitiesByYear = mainController.getActivitiesByYear(selectedActivity.value)
                                if (activitiesByYear[year.toString()] != null) {
                                    // checkboxes to activate/deactivate year series charts
                                    checkbox("$year") {
                                        isSelected = true
                                        action {
                                            if (isSelected) {
                                                activeYearsSet.add(this.text)
                                            } else {
                                                activeYearsSet.remove(this.text)
                                            }
                                            multipleAxesLineChartPane = cumulativeDistancePerYear(selectedActivity.value)
                                            borderPane.center.replaceChildren(multipleAxesLineChartPane)
                                        }
                                    }
                                } else {
                                    activeYearsSet.remove(year.toString())
                                    continue
                                }
                            }
                        }
                    }
                    center {
                        borderPane = this
                        multipleAxesLineChartPane = cumulativeDistancePerYear(selectedActivity.value)
                        multipleAxesLineChartPane.attachTo(this)
                    }
                }
            }
            item("${selectedActivity.value} Eddington number") {
                eddingtonNumberChart(mainController.getActiveDaysByActivityType(selectedActivity.value))
            }
        }
    }

    private inner class DetailsPopup : VBox() {

        init {
            style =
                "-fx-border-width: 1px; -fx-padding: 5 5 5 5px; -fx-border-color: gray; -fx-background-color: whitesmoke;"
            isVisible = false
        }

        fun famousClimbDescription(badge: FamousClimbBadge) {
            children.clear()
            children.add(Label(badge.label))
            children.add(Label("top : ${badge.topOfTheAscent} m"))
            children.add(Label("total ascent : ${badge.totalAscent} m"))
            children.add(Label("length : ${badge.length} km"))
            children.add(Label("avg. gradient : ${badge.averageGradient} %"))
            children.add(Label("difficulty points : ${badge.difficulty}"))
        }
    }

    private fun <ROW, T : Double?> formatDistance(): Callback<TableColumn<ROW, T>?, TableCell<ROW, T>> {
        return Callback<TableColumn<ROW, T>?, TableCell<ROW, T>> {
            object : TableCell<ROW, T>() {
                override fun updateItem(item: T?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null || empty) {
                        setText(null)
                    } else {
                        setText("%.02f km".format(item.div(1000)))
                    }
                }
            }
        }
    }

    private fun <ROW, T : Int?> formatSeconds(): Callback<TableColumn<ROW, T>?, TableCell<ROW, T>> {
        return Callback<TableColumn<ROW, T>?, TableCell<ROW, T>> {
            object : TableCell<ROW, T>() {
                override fun updateItem(item: T?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null || empty) {
                        setText(null)
                    } else {
                        setText(item.formatSeconds())
                    }
                }
            }
        }
    }

    private fun <ROW, T : Double?> formatElevation(): Callback<TableColumn<ROW, T>?, TableCell<ROW, T>> {
        return Callback<TableColumn<ROW, T>?, TableCell<ROW, T>> {
            object : TableCell<ROW, T>() {
                override fun updateItem(item: T?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null || empty) {
                        setText(null)
                    } else {
                        setText("%.0f m".format(item))
                    }
                }
            }
        }
    }

    private fun <ROW, T : Double?> formatSpeed(activityType: String): Callback<TableColumn<ROW, T>?, TableCell<ROW, T>> {
        return Callback<TableColumn<ROW, T>?, TableCell<ROW, T>> {
            object : TableCell<ROW, T>() {
                override fun updateItem(item: T, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null || empty) {
                        setText(null)
                    } else {
                        setText(item.formatSpeed(activityType))
                    }
                }
            }
        }
    }

    private fun buildFamousClimbBadgesSetScrollpane(famousClimbBadgesSetToDisplay: List<List<BadgeDisplay>>): ScrollPane {
        return scrollpane(fitToWidth = true) {
            flowpane {
                for (badgeSetToDisplay in famousClimbBadgesSetToDisplay) {
                    vgap = 15.0
                    hgap = 15.0
                    for (badgeToDisplay in badgeSetToDisplay) {
                        borderpane {
                            bottom = text {
                                text = badgeToDisplay.label
                                borderpaneConstraints {
                                    alignment = Pos.CENTER
                                }
                            }

                            badgeToDisplay.activity?.onMouseEntered = EventHandler {
                                detailsPopup.isVisible = true
                            }
                            badgeToDisplay.activity?.onMouseExited = EventHandler {
                                detailsPopup.isVisible = false
                            }
                            badgeToDisplay.activity?.onMouseMoved = EventHandler { event: MouseEvent ->
                                val x = event.sceneX
                                val y = event.sceneY

                                detailsPopup.famousClimbDescription(badgeToDisplay.badge as FamousClimbBadge)

                                if (y + detailsPopup.height + 10 < height) {
                                    AnchorPane.setTopAnchor(detailsPopup, y + 10)
                                } else {
                                    AnchorPane.setTopAnchor(detailsPopup, y - 10 - detailsPopup.height)
                                }

                                if (x + detailsPopup.width + 10 < width) {
                                    AnchorPane.setLeftAnchor(detailsPopup, x + 10)
                                } else {
                                    AnchorPane.setLeftAnchor(detailsPopup, x - 10 - detailsPopup.width)
                                }
                                detailsPopup.isVisible = true
                            }

                            center = badgeToDisplay.activity
                        }
                    }
                }
            }
        }
    }

    private fun buildGeneralBadgesSetScrollpane(generalBadgesSetToDisplay: List<List<BadgeDisplay>>): ScrollPane {
        return scrollpane(fitToWidth = true) {
            flowpane {
                for (badgeSetToDisplay in generalBadgesSetToDisplay) {
                    vgap = 15.0
                    hgap = 15.0
                    for (badgeToDisplay in badgeSetToDisplay) {
                        borderpane {
                            bottom = text {
                                text = badgeToDisplay.label
                                borderpaneConstraints {
                                    alignment = Pos.CENTER
                                }
                            }
                            center = badgeToDisplay.activity
                        }
                    }
                }
            }
        }

    }

    private fun cumulativeDistancePerYear(activityType: String): Pane {

        val activitiesByYear = mainController.getActivitiesByYear(activityType)
        val allSeries = mutableListOf<XYChart.Series<String, Number>>()
        for (year in 2010..LocalDate.now().year) {
            val activities = if (activitiesByYear[year.toString()] != null && this.activeYearsSet.contains(year.toString())) {
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
            MultipleLineChart("$activityType distance (km) by years", allSeries)
        }
    }

    private fun distancePerYear(activityType: String): Pane {

        val activitiesByYear = mainController.getActivitiesByYear(activityType)
        val allSeries = mutableListOf<XYChart.Series<String, Number>>()
        for (year in 2010..LocalDate.now().year) {
            val activities = if (activitiesByYear[year.toString()] != null) {
                activitiesByYear[year.toString()]!!
            } else {
                continue
            }
            val activitiesPerMonth = ActivityHelper.groupActivitiesByMonth(activities)
            val cumulativeDistance = ActivityHelper.sumDistanceByType(activitiesPerMonth, activityType)
            val data = cumulativeDistance.entries.map { entry ->
                XYChart.Data<String, Number>(entry.key, entry.value)
            }.toObservable()
            allSeries.add(XYChart.Series(year.toString(), data))
        }
        return if (allSeries.isEmpty()) {
            Pane()
        } else {
            return MultipleLineChart("$activityType distance (km) per years", allSeries)
        }
    }
}