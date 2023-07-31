package me.nicolas.stravastats.ihm

import com.sothawo.mapjfx.Configuration
import com.sothawo.mapjfx.Coordinate
import com.sothawo.mapjfx.CoordinateLine
import com.sothawo.mapjfx.MapView
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
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
import javafx.scene.paint.Color
import javafx.util.Callback
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.nicolas.stravastats.business.*
import me.nicolas.stravastats.business.badges.FamousClimbBadge
import me.nicolas.stravastats.ihm.chart.MultipleLineChart
import me.nicolas.stravastats.ihm.chart.MultipleLineChart.Companion.COLORS
import me.nicolas.stravastats.ihm.chart.eddingtonNumberChart
import me.nicolas.stravastats.service.ActivityHelper
import me.nicolas.stravastats.utils.formatSeconds
import me.nicolas.stravastats.utils.formatSpeed
import tornadofx.*
import java.time.LocalDate


class MainView(
    clientId: String,
    athlete: Athlete?,
    activities: ObservableList<Activity>
) : View("MyStravaStats") {

    companion object {
        const val overYears = "Over years"
    }

    override val root = borderpane {
        setPrefSize(1200.0, 800.0)
    }

    private val mainController: MainController = MainController(clientId, activities)

    private var selectedYear = SimpleStringProperty("${LocalDate.now().year}")
    private var selectedActivity = selectFirstDisplayActivity(activities)

    private var selectedActivityLabel: Label by singleAssign()

    private var statisticsTab: Tab by singleAssign()
    private var activitiesTab: Tab by singleAssign()
    private var chartsTab: Tab by singleAssign()
    private var badgesTab: Tab by singleAssign()

    // To manage year selection in MultipleLineChart
    private lateinit var multipleAxesLineChartPane: Pane
    private lateinit var borderPane: BorderPane

    private val detailsWindow: AnchorPane
    private val detailsPopup: DetailsPopup

    private val activeYearsSet = (LocalDate.now().year downTo 2010).map { "$it" }.toMutableSet()

    private var coordinateLines: Collection<CoordinateLine> = ArrayList()

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
                            selectedActivityLabel = label(selectedActivity.value) {
                                prefWidth = 150.0
                                alignment = Pos.CENTER
                            }
                            combobox(
                                property = selectedYear,
                                values = (LocalDate.now().year downTo 2010).map { "$it" }.toMutableList()
                                    .apply { this.add(0, overYears) }
                            ) {
                                selectionModel.selectedItemProperty().onChange {
                                    updateMainView()
                                }
                                prefWidth = 150.0
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
                                tooltip("Ride")
                                action {
                                    selectedActivity = SimpleStringProperty(Ride)
                                    updateMainView()
                                }
                            }
                            button {
                                imageview("images/buttons/commute.png")
                                tooltip("Commute")
                                action {
                                    selectedActivity = SimpleStringProperty(Commute)
                                    updateMainView()
                                }
                            }
                            button {
                                imageview("images/buttons/run.png")
                                tooltip("Run")
                                action {
                                    selectedActivity = SimpleStringProperty(Run)
                                    updateMainView()
                                }
                            }
                            button {
                                imageview("images/buttons/inlineskate.png")
                                tooltip("Inline Skate")
                                action {
                                    selectedActivity = SimpleStringProperty(InlineSkate)
                                    updateMainView()
                                }
                            }
                            button {
                                imageview("images/buttons/hike.png")
                                tooltip("Hike")
                                action {
                                    selectedActivity = SimpleStringProperty(Hike)
                                    updateMainView()
                                }
                            }
                            button {
                                imageview("images/buttons/alpineski.png")
                                tooltip("Alpine Ski")
                                action {
                                    selectedActivity = SimpleStringProperty("AlpineSki")
                                    updateMainView()
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
                                tooltip("Generate CSV")
                                action {
                                    mainController.generateCSV(getSelectedYear())
                                }
                            }
                            button {
                                imageview("images/buttons/charts.png")
                                tooltip("Generate charts")
                                action {
                                    mainController.generateCharts(getSelectedYear())
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

        updateMainView()
    }

    private fun getSelectedYear() = if (selectedYear.value == overYears) {
        null
    } else {
        selectedYear.value.toInt()
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

    private fun updateMainView() {
        val selectedYearValue = getSelectedYear()
        val statisticsToDisplay = mainController.getStatisticsToDisplay(selectedActivity.value, selectedYearValue)
        val activitiesToDisplay = mainController.getActivitiesToDisplay(selectedActivity.value, selectedYearValue)
        val generalBadgesSetToDisplay = mainController.getGeneralBadgesSetToDisplay(selectedActivity.value, selectedYearValue)

        // update main view
        selectedActivityLabel.text = selectedActivity.value

        activitiesTab.content = tableview(activitiesToDisplay) {
            readonlyColumn("Activity", ActivityDisplay::name)
            readonlyColumn("Distance", ActivityDisplay::distance).cellFactory = formatDistance()
            readonlyColumn("Elapsed time", ActivityDisplay::elapsedTime).cellFactory = formatSeconds()
            readonlyColumn("Total elevation gain", ActivityDisplay::totalElevationGain).cellFactory = formatElevation()
            readonlyColumn("Total descent", ActivityDisplay::totalDescent).cellFactory = formatElevation()
            readonlyColumn("Average speed", ActivityDisplay::averageSpeed).cellFactory = formatSpeed(selectedActivity.value)
            readonlyColumn("Best speed for 1000 m", ActivityDisplay::bestTimeForDistanceFor1000m)
            if (selectedActivity.value != AlpineSki) {
                readonlyColumn("Max gradient for 250 m", ActivityDisplay::bestElevationForDistanceFor250m)
                readonlyColumn("Max gradient for 500 m", ActivityDisplay::bestElevationForDistanceFor500m)
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
            if (selectedYearValue != null) {
                item("Distance by m...", expanded = true) {
                    barchart("Distance by months for $selectedYearValue (km)", CategoryAxis(), NumberAxis()) {
                        series(
                            selectedActivity.value,
                            mainController.buildDistanceByMonthsSeries(selectedActivity.value, selectedYearValue)
                        )
                        verticalGridLinesVisible = false
                        isLegendVisible = false
                    }
                }
                item("Elevation by m...") {
                    barchart("Elevation gain by months for $selectedYearValue (m)", CategoryAxis(), NumberAxis()) {
                        series(
                            selectedActivity.value,
                            mainController.buildElevationGainByMonthsSeries(selectedActivity.value, selectedYearValue)
                        )
                        verticalGridLinesVisible = false
                        isLegendVisible = false
                    }
                }
                item("Distance by w...") {
                    barchart("Distance by weeks for $selectedYearValue (km)", CategoryAxis(), NumberAxis()) {
                        series(
                            selectedActivity.value,
                            mainController.buildDistanceByWeeksSeries(selectedActivity.value, selectedYearValue)
                        )
                        verticalGridLinesVisible = false
                        isLegendVisible = false
                    }
                }
                item("Elevation by w...") {
                    barchart("Elevation gain by weeks for $selectedYearValue (m)", CategoryAxis(), NumberAxis()) {
                        series(
                            selectedActivity.value,
                            mainController.buildElevationGainByWeeksSeries(selectedActivity.value, selectedYearValue)
                        )
                        verticalGridLinesVisible = false
                        isLegendVisible = false
                    }
                }
                item("Distance by d...") {
                    barchart("Distance by days for $selectedYearValue (km)", CategoryAxis(), NumberAxis()) {
                        series(
                            selectedActivity.value,
                            mainController.buildDistanceByDaysSeries(selectedActivity.value, selectedYearValue)
                        )
                        verticalGridLinesVisible = false
                        isLegendVisible = false
                    }
                }
                if (selectedActivity.value !in listOf(AlpineSki, Commute)) {
                    item("Eddington number") {
                        eddingtonNumberChart(
                            mainController.getActiveDaysByActivityTypeByYear(selectedActivity.value, selectedYearValue)
                        )
                    }
                }

            } else {
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
                                    val activitiesByYear =
                                        mainController.getActivitiesByYear(selectedActivity.value)
                                    if (activitiesByYear[year.toString()] != null) {
                                        // checkboxes to activate/deactivate year series charts
                                        checkbox("$year") {
                                            textFill = COLORS["$year"]
                                            isSelected = true
                                            action {
                                                if (isSelected) {
                                                    activeYearsSet.add(this.text)
                                                } else {
                                                    activeYearsSet.remove(this.text)
                                                }
                                                multipleAxesLineChartPane =
                                                    cumulativeDistancePerYear(selectedActivity.value)
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
            item("All tracks") {
                val allTracksMapView = MapView()
                // set the custom css file for the MapView
                allTracksMapView.setCustomMapviewCssURL(javaClass.getResource("/mapview.css"))

                // finally, initialize the map view
                allTracksMapView.initialize(
                    Configuration.builder()
                        .showZoomControls(false)
                        .build()
                )
                // watch the MapView's initialized property to finish initialization
                allTracksMapView.initializedProperty()
                    .addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
                        if (newValue) {
                            afterMapIsInitialized(selectedActivity.value, getSelectedYear(), allTracksMapView)
                        }
                    }

                children.add(allTracksMapView)
            }
        }
        badgesTab.content = drawer {
            item("General", expanded = true) {
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
            if (selectedActivity.value == "Ride") {
                val famousClimbBadgesSetToDisplay =
                    mainController.getFamousClimbBadgesSetToDisplay(selectedActivity.value, getSelectedYear())
                item("Famous climb") {
                    this.add(buildFamousClimbBadgesSetScrollpane(famousClimbBadgesSetToDisplay))
                }
            } else {
                item("Famous climb") {
                    scrollpane(fitToWidth = true) {
                        flowpane {
                            // Nothing to display
                        }
                    }
                }
            }
        }
    }

    private fun afterMapIsInitialized(activityType: String, selectedYearValue: Int?, mapView: MapView) {

        runBlocking {
            launch {
                val filteredActivities = mainController.getFilteredActivities(activityType, selectedYearValue)

                // Take 1 out 10 points for this map
                coordinateLines = filteredActivities.mapNotNull { activity ->
                    val coordinates = activity.stream?.latitudeLongitude?.data?.map { Coordinate(it[0], it[1]) }
                        ?.windowed(1, 10)
                        ?.flatten()

                    if (!coordinates.isNullOrEmpty()) {
                        CoordinateLine(coordinates)
                            .setColor(Color.MAGENTA)
                            .setVisible(true)
                    } else {
                        null
                    }
                }

                coordinateLines.forEach { coordinateLine ->
                    mapView.addCoordinateLine(coordinateLine)
                }

            }
        }

        if (coordinateLines.isNotEmpty()) {
            val firstTrack = coordinateLines.first()
            val coordinateOfFirstTrack = firstTrack.coordinateStream.toList()
            if (coordinateOfFirstTrack.isNotEmpty()) {
                mapView.center =
                    Coordinate(coordinateOfFirstTrack.first().latitude, coordinateOfFirstTrack.first().longitude)
            } else {
                // Rennes
                mapView.center = Coordinate(48.1606, -1.5395)
            }
        } else {
            // Rennes
            mapView.center = Coordinate(48.1606, -1.5395)
        }
        mapView.zoom -= 3.0
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

    private fun cumulativeDistancePerYear(activityType: String): Pane {

        val activitiesByYear = mainController.getActivitiesByYear(activityType)
        val allSeries = mutableListOf<XYChart.Series<String, Number>>()
        for (year in 2010..LocalDate.now().year) {
            val activities =
                if (activitiesByYear[year.toString()] != null && this.activeYearsSet.contains(year.toString())) {
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