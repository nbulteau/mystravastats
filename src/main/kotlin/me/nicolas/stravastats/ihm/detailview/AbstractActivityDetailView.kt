package me.nicolas.stravastats.ihm.detailview

import com.sothawo.mapjfx.*
import javafx.animation.Transition
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.chart.AreaChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.ToggleGroup
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.util.Duration
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.GeoCoordinate
import me.nicolas.stravastats.business.SegmentEffort
import me.nicolas.stravastats.service.statistics.calculateBestTimeForDistance
import me.nicolas.stravastats.utils.formatSeconds
import me.nicolas.stravastats.utils.formatSpeed
import me.nicolas.stravastats.utils.inDateTimeFormatter
import me.nicolas.stravastats.utils.timeFormatter
import tornadofx.*
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.round
import kotlin.math.roundToInt


abstract class AbstractActivityDetailView(
    protected val activity: Activity,
    protected val latitudeLongitudesList: List<List<Double>>,
    protected val distancesList: List<Double>,
    protected val altitudesList: List<Double>,
    private val segmentEfforts: List<SegmentEffort>
) : View(activity.toString()) {

    companion object {
        val statsList = listOf(500.0, 1000.0, 5000.0, 10000.0)
    }

    private val markersCreatedOnClick = mutableMapOf<String, Marker>()

    override val root = borderpane {
        setPrefSize(1200.0, 800.0)
    }

    private val activityTrack: CoordinateLine?

    protected val statsTracks = buildSpeedStatsTracks().toMutableList()

    private val segmentsTracks = buildSegmentsTracks()

    private val mapView = MapView()

    private lateinit var areaChart: AreaChart<Number, Number>

    private val detailsWindow: AnchorPane

    private var marker: Marker? = null

    init {
        FX.primaryStage.isResizable = true

        activityTrack = if (activity.stream?.latitudeLongitude != null) {
            CoordinateLine(latitudeLongitudesList.map { coord ->
                Coordinate(coord[0], coord[1])
            }).setColor(Color.MAGENTA).setVisible(true)
        } else {
            null
        }

        detailsWindow = AnchorPane()
        buildAltitudeAreaChart()
    }

    private fun buildAltitudeAreaChart() {
        if (latitudeLongitudesList.isNotEmpty()) {
            val minAltitude = max(round(altitudesList.minOf { it } - 10), 0.0)
            val maxAltitude = round(altitudesList.maxOf { it } + 20)
            val maxDistance = distancesList.maxOf { it } / 1000

            val xAxis = NumberAxis(0.0, maxDistance, 1.0)
            val yAxis = NumberAxis(minAltitude, maxAltitude, 10.0)
            areaChart = areachart("Altitude", xAxis, yAxis) {
                val data = distancesList //.windowed(1, 10).flatten()
                    .zip(altitudesList) { distance, altitude ->
                        XYChart.Data<Number, Number>(distance / 1000, altitude)
                    }.toObservable()


                if (maxDistance > 30.0) {
                    xAxis.tickUnit = 10.0
                }
                if (maxAltitude - minAltitude > 500.0) {
                    yAxis.tickUnit = 100.0
                    yAxis.isMinorTickVisible = true
                }
                if (maxAltitude - minAltitude > 1000.0) {
                    yAxis.tickUnit = 200.0
                    yAxis.isMinorTickVisible = false
                }

                this.isAlternativeRowFillVisible = false
                this.isLegendVisible = false
                this.createSymbols = false
                this.data.add(XYChart.Series("", data))
            }
        }
    }

    private fun buildSpeedStatsTracks(): List<StatsTrack> {
        return statsList.mapNotNull { distanceStat ->
            val activityEffort = activity.calculateBestTimeForDistance(distanceStat)
            if (activityEffort != null) {
                val coordinateLine =
                    CoordinateLine(latitudeLongitudesList.mapIndexedNotNull { index, coord ->
                        if (index >= activityEffort.idxStart && index <= activityEffort.idxEnd) {
                            Coordinate(coord[0], coord[1])
                        } else {
                            null
                        }
                    }).setColor(Color.BLUE).setWidth(5)

                val xyChartSeries = XYChart.Series(distancesList
                    .zip(altitudesList) { distance, altitude ->
                        XYChart.Data<Number, Number>(distance / 1000, altitude)
                    }
                    .subList(activityEffort.idxStart, activityEffort.idxEnd)
                    .toObservable())

                StatsTrack(coordinateLine, xyChartSeries, activityEffort, StatType.SPEED)
            } else {
                null
            }
        }
    }

    private fun buildSegmentsTracks(): List<SegmentTrack> {
        return segmentEfforts.map { segmentEffort ->
            val coordinateLine = CoordinateLine(latitudeLongitudesList.mapIndexedNotNull { index, coord ->
                if (index >= segmentEffort.startIndex && index <= segmentEffort.endIndex) {
                    Coordinate(coord[0], coord[1])
                } else {
                    null
                }
            }).setColor(Color.ORANGE).setWidth(5)

            val xyChartSeries = XYChart.Series(distancesList
                .zip(altitudesList) { distance, altitude ->
                    XYChart.Data<Number, Number>(distance / 1000, altitude)
                }
                .subList(segmentEffort.startIndex, segmentEffort.endIndex)
                .toObservable())

            val deltaAltitude = altitudesList[segmentEffort.endIndex] - altitudesList[segmentEffort.startIndex]
            val formattedGradient = "%.02f %%".format(100 * deltaAltitude / segmentEffort.distance)

            SegmentTrack(
                coordinateLine,
                xyChartSeries,
                segmentEffort,
                segmentEffort.getFormattedSpeed(activity.type),
                formattedGradient
            )
        }
    }

    private fun buildBorderPane() {
        with(root) {

            top {
                hbox(alignment = Pos.CENTER) {
                    label("Distance : %.02f km".format(activity.distance / 1000))
                    label("|")
                    label("Total elevation gain : %.0f m".format(activity.totalElevationGain))
                    label("|")
                    label("Moving time : ${activity.elapsedTime.formatSeconds()}")
                    label("|")
                    label("Average speed : ${activity.averageSpeed.formatSpeed(activity.type)}")
                    children.style {
                        fontWeight = FontWeight.BOLD
                        font = Font.font("Verdana", 10.0)
                        padding = box(5.px)
                    }
                }
            }

            center {
                useMaxHeight = true
                useMaxWidth = true
                mapView.prefHeight(850.0)
                padding = Insets(5.0, 10.0, 5.0, 5.0)
                center = mapView
            }

            bottom {
                vbox(alignment = Pos.CENTER) {
                    add(areaChart)
                    add(detailsWindow)
                    maxHeight = 200.0
                }
            }

            right {
                val toggleGroup = ToggleGroup()
                scrollpane {
                    setPrefSize(305.0, 300.0)
                    isFitToWidth = true
                    isFitToHeight = true
                    hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                    vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

                    vbox {
                        (statsTracks + segmentsTracks).forEach { statsTracks ->
                            radiobutton(statsTracks.toString(), toggleGroup) {
                                tooltip {
                                    text = statsTracks.buildTooltipText()
                                    showDelay = Duration(500.0)
                                    showDuration = Duration(10_000.0)
                                }
                                action {
                                    showTrack(statsTracks)
                                }
                            }
                        }

                        children.style {
                            fontWeight = FontWeight.BOLD
                            font = Font.font("Verdana", 10.0)
                            padding = box(5.px)
                        }
                    }
                }
            }
        }
    }

    private fun showTrack(displayTrack: DisplayTrack) {
        statsTracks.forEach { statsTrack -> statsTrack.track.setVisible(false) }
        segmentsTracks.forEach { segmentTrack -> segmentTrack.track.setVisible(false) }
        displayTrack.track.setVisible(true)

        val mainTrackAreaChart = areaChart.data.first()
        areaChart.data =
            listOf<XYChart.Series<Number, Number>>(mainTrackAreaChart, displayTrack.altitudeAreaChart).toObservable()
    }

    private fun calculateInstantSpeed(index: Int): Double? {
        // Speed
        val coordinate1 = latitudeLongitudesList.getOrNull(index - 1)
        val coordinate2 = latitudeLongitudesList.getOrNull(index)
        return if (coordinate1 != null && coordinate2 != null) {
            val start = GeoCoordinate(coordinate1[0], coordinate1[1])
            val distanceBetweenCoordinates = start.haversineInM(coordinate2[0], coordinate2[1])
            val timeBetweenCoordinates =
                activity.stream?.time?.data?.get(index)!! - activity.stream?.time?.data?.get(index - 1)!!
            distanceBetweenCoordinates.toDouble() / timeBetweenCoordinates
        } else {
            null
        }
    }

    protected fun initMapView() {

        buildBorderPane()

        // set the custom css file for the MapView
        mapView.setCustomMapviewCssURL(javaClass.getResource("/mapview.css"))

        // finally, initialize the map view
        mapView.initialize(
            Configuration.builder().showZoomControls(false).build()
        )
        // watch the MapView's initialized property to finish initialization
        mapView.initializedProperty().addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            if (newValue) {
                afterMapIsInitialized()
            }
        }

        bindMouseEvents()
    }

    private fun bindMouseEvents() {

        val strokeWidth = 1.5

        this.areaChart.add(detailsWindow)

        val detailsPopup = DetailsPopup()
        detailsWindow.children.add(detailsPopup)
        detailsWindow.isMouseTransparent = true

        this.areaChart.onMouseMoved = null
        this.areaChart.isMouseTransparent = false

        val yAxis = this.areaChart.yAxis

        val yLine = Line()
        yLine.fill = Color.GRAY
        yLine.strokeWidth = strokeWidth / 2
        yLine.isVisible = false

        val chartBackground = this.areaChart.lookup(".chart-plot-background")
        for (node in chartBackground.parent.childrenUnmodifiable) {
            if (node !== chartBackground && node !== yAxis) {
                node.isMouseTransparent = true
            }
        }

        chartBackground.cursor = Cursor.DEFAULT

        chartBackground.onMouseEntered = EventHandler { event: MouseEvent ->
            chartBackground.onMouseMoved.handle(event)
            detailsPopup.isVisible = true
            yLine.isVisible = true
            marker?.visible = true
            detailsWindow.children.add(yLine)
        }

        chartBackground.onMouseExited = EventHandler {
            detailsPopup.isVisible = false
            yLine.isVisible = false
            marker?.visible = false
            detailsWindow.children.remove(yLine)
        }

        chartBackground.onMouseMoved = EventHandler { event: MouseEvent ->
            val x = event.x + chartBackground.layoutX
            val y = event.y + chartBackground.layoutY

            yLine.startX = x + 3
            yLine.endX = x + 3
            yLine.startY = this.areaChart.height - 30
            yLine.endY = detailsWindow.height + 30

            if (this.areaChart.xAxis.getValueForDisplay(event.x) != null) {
                detailsPopup.showChartDescription(event.x)
                if (y + detailsPopup.height + 10 < this.areaChart.height) {
                    AnchorPane.setTopAnchor(detailsPopup, y + 10)
                } else {
                    AnchorPane.setTopAnchor(detailsPopup, y - 10 - detailsPopup.height)
                }

                if (x + detailsPopup.width + 10 < this.areaChart.width) {
                    AnchorPane.setLeftAnchor(detailsPopup, x + 10)
                } else {
                    AnchorPane.setLeftAnchor(detailsPopup, x - 10 - detailsPopup.width)
                }
                detailsPopup.isVisible = true
            } else {
                detailsPopup.isVisible = false
            }

            // manage marker on the mapView
            val newPosition: Coordinate? = getMarkerPosition(event.x)
            if (newPosition != null) {
                marker?.position = newPosition
                // adding can only be done after coordinate is set
                mapView.addMarker(marker)
            }
        }
    }

    private fun getMarkerPosition(displayPosition: Double): Coordinate? {
        val xValue: Number = areaChart.xAxis.getValueForDisplay(displayPosition)
        val index = findStreamDataIndex(xValue)
        if (index != null) {
            val latitudeLongitude = latitudeLongitudesList[index]
            return Coordinate(latitudeLongitude[0], latitudeLongitude[1])
        }
        return null
    }

    private fun findStreamDataIndex(xValue: Number): Int? {
        val xValueRounded = (xValue.toDouble() * 10).roundToInt() / 10.0
        var index = 0
        while (index < areaChart.data[0].data.size) {
            val rounded = (areaChart.data[0].data[index].xValue.toDouble() * 10).roundToInt() / 10.0
            if (rounded == xValueRounded) {
                return index
            }
            index++
        }
        return null
    }

    override fun onDock() {
        currentWindow?.setOnCloseRequest { windowEvent ->
            windowEvent.consume()
            this.closeActivityDetailView()
        }
    }

    private fun closeActivityDetailView() {
        mapView.close()
        super.close()
    }

    /**
     * finishes setup after the mpa is initialized
     */
    private fun afterMapIsInitialized() {
        if (activityTrack != null) {
            // add tracks
            mapView.addCoordinateLine(activityTrack)
            for (segmentTrack in segmentsTracks) {
                mapView.addCoordinateLine(segmentTrack.track)
            }
            for (statsTrack in statsTracks) {
                mapView.addCoordinateLine(statsTrack.track)
            }

            // get the extent for track
            val tracksExtent = Extent.forCoordinates(activityTrack.coordinateStream.toList())

            // add start & end makers and position marker
            addMarkers()

            // set  bounds and fix zoom
            mapView.setExtent(tracksExtent)
            //mapView.zoom -= 1.0

            // Set the focus
            mapView.center = tracksExtent.getCenter()
        }
    }

    private fun addMarkers() {
        val start = latitudeLongitudesList.first()
        val startPNGURL = javaClass.getResource("/images/startmarker.png")
        val startMarker = Marker(startPNGURL, -16, -16)
        startMarker.position = Coordinate(start[0], start[1])
        startMarker.visible = true

        mapView.addMarker(startMarker)
        markersCreatedOnClick[startMarker.id] = startMarker

        val end = latitudeLongitudesList.last()

        val finishPNGURL = javaClass.getResource("/images/finishmarker.png")
        val endMarker = Marker(finishPNGURL, 0, -32)
        endMarker.position = Coordinate(end[0], end[1])
        endMarker.visible = true

        mapView.addMarker(endMarker)
        markersCreatedOnClick[endMarker.id] = endMarker

        marker = Marker.createProvided(Marker.Provided.BLUE).setVisible(false)

        mapView.addMarker(marker)
    }

    /**
     * @return the center of the Extent
     */
    private fun Extent.getCenter(): Coordinate {
        val northWest = this.min
        val southEast = this.max
        val latitude: Double = (northWest.latitude + southEast.latitude) * 0.5
        val longitude: Double = (northWest.longitude + southEast.longitude) * 0.5

        return Coordinate(latitude, longitude)
    }

    private inner class DetailsPopup : VBox() {

        init {
            style =
                "-fx-border-width: 1px; -fx-padding: 5 5 5 5px; -fx-border-color: gray; -fx-background-color: whitesmoke;"
            isVisible = false
        }

        fun showChartDescription(displayPosition: Double) {
            children.clear()
            children.add(buildPopupRow(displayPosition))
        }

        private fun buildPopupRow(displayPosition: Double): VBox {
            val xValue: Number = areaChart.xAxis.getValueForDisplay(displayPosition)

            val vbox = VBox()
            val index = findStreamDataIndex(xValue)
            if (index != null) {
                val time = activity.stream?.time?.data?.get(index)?.toLong()!!
                val datetime = LocalDateTime.parse(activity.startDateLocal, inDateTimeFormatter)
                val timeValueLabel = Label(datetime.plusSeconds(time).format(timeFormatter))
                vbox.add(HBox(10.0, timeValueLabel))

                // Distance
                val distance = activity.stream?.distance?.data?.get(index)
                val distanceLabel = Label("Distance:")
                val distanceValueLabel = Label(String.format("%.1f km", distance?.div(1000)))
                distanceValueLabel.font = Font.font("Arial", FontWeight.EXTRA_BOLD, 15.0)
                vbox.add(HBox(10.0, distanceLabel, distanceValueLabel))

                // Speed
                val coordinate1 = latitudeLongitudesList.getOrNull(index - 1)
                val coordinate2 = latitudeLongitudesList.getOrNull(index)
                val speedLabel = Label("Speed:")
                val speedValueLabel: Label = if (coordinate1 != null && coordinate2 != null) {
                    val start = GeoCoordinate(coordinate1[0], coordinate1[1])
                    val distanceBetweenCoordinates = start.haversineInM(coordinate2[0], coordinate2[1])
                    val timeBetweenCoordinates =
                        activity.stream?.time?.data?.get(index)!! - activity.stream?.time?.data?.get(index - 1)!!
                    val speed = distanceBetweenCoordinates.toDouble() / timeBetweenCoordinates
                    Label(speed.formatSpeed(activity.type))
                } else {
                    Label("-")
                }
                speedValueLabel.font = Font.font("Arial", FontWeight.EXTRA_BOLD, 15.0)
                vbox.add(HBox(10.0, speedLabel, speedValueLabel))

                // Gradient
                val altitude1 = activity.stream?.altitude?.data?.getOrNull(index - 5)
                val altitude2 = activity.stream?.altitude?.data?.getOrNull(index)
                val gradientLabel = Label("Gradient:")
                val gradientValueLabel: Label =
                    if (coordinate1 != null && coordinate2 != null && altitude1 != null && altitude2 != null) {
                        val elevationDiff = altitude2 - altitude1
                        val distance1 = activity.stream?.distance?.data?.get(index - 5)
                        val distance2 = activity.stream?.distance?.data?.get(index)
                        val distanceBetweenCoordinates = distance2!! - distance1!!
                        val gradient = elevationDiff / distanceBetweenCoordinates
                        Label("%.2f %%".format(gradient * 100))
                    } else {
                        Label("-")
                    }
                gradientValueLabel.font = Font.font("Arial", FontWeight.EXTRA_BOLD, 15.0)
                vbox.add(HBox(10.0, gradientLabel, gradientValueLabel))

                // Altitude
                val altitude = activity.stream?.altitude?.data?.get(index)
                val altitudeLabel = Label("Altitude:")
                val altitudeValueLabel = Label("%d m".format(altitude?.toInt()))
                altitudeValueLabel.font = Font.font("Arial", FontWeight.EXTRA_BOLD, 15.0)
                vbox.add(HBox(10.0, altitudeLabel, altitudeValueLabel))
            }

            return vbox
        }
    }

    /**
     * Animate the marker to the new position
     */
    private fun animateMarker(oldPosition: Coordinate, newPosition: Coordinate) {
        val transition: Transition = object : Transition() {
            private val oldPositionLongitude = oldPosition.longitude
            private val oldPositionLatitude = oldPosition.latitude
            private val deltaLatitude = newPosition.latitude - oldPositionLatitude
            private val deltaLongitude = newPosition.longitude - oldPositionLongitude

            init {
                cycleDuration = Duration.seconds(1.0)
                setOnFinished { marker?.position = newPosition }
            }

            override fun interpolate(v: Double) {
                val latitude = oldPosition.latitude + v * deltaLatitude
                val longitude = oldPosition.longitude + v * deltaLongitude
                marker?.position = Coordinate(latitude, longitude)
            }
        }
        transition.play()
    }
}

