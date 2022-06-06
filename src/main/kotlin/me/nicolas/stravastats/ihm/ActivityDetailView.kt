package me.nicolas.stravastats.ihm

import com.sothawo.mapjfx.*
import javafx.animation.Transition
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.chart.AreaChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Label
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
import me.nicolas.stravastats.service.formatSeconds
import me.nicolas.stravastats.service.formatSpeed
import me.nicolas.stravastats.service.inDateTimeFormatter
import me.nicolas.stravastats.service.timeFormatter
import tornadofx.*
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round
import kotlin.math.roundToInt


class ActivityDetailView(val activity: Activity) : View(activity.toString()) {

    private val markersCreatedOnClick = mutableMapOf<String, Marker>()

    override val root = borderpane {
        setPrefSize(1024.0, 768.0)
    }

    private val track: CoordinateLine?

    private val mapView = MapView()

    private lateinit var areaChart: AreaChart<Number, Number>

    private val detailsWindow: AnchorPane

    private lateinit var marker: Marker

    init {
        FX.primaryStage.isResizable = true

        track = if (activity.stream?.latitudeLongitude != null) {
            CoordinateLine(activity.stream?.latitudeLongitude?.data?.map { Coordinate(it[0], it[1]) })
                .setColor(Color.MAGENTA)
                .setVisible(true)
        } else {
            null
        }

        detailsWindow = AnchorPane()

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
            center = mapView
            bottom {
                val stream = activity.stream
                if (stream?.altitude != null && stream.altitude.data.isNotEmpty()) {
                    val minAltitude = max(round(((stream.altitude.data.minOf { it } - 20) / 10)) * 10, 0.0)
                    val maxAltitude = round(((stream.altitude.data.maxOf { it } + 10) / 10)) * 10
                    val maxDistance = stream.distance.data.maxOf { it } / 1000

                    vbox {
                        val xAxis = NumberAxis(0.0, maxDistance, 1.0)
                        val yAxis = NumberAxis(minAltitude, maxAltitude, 10.0)
                        areaChart = areachart("Altitude", xAxis, yAxis) {
                            val data =
                                stream.distance.data //.windowed(1, 10).flatten()
                                    .zip(stream.altitude.data) { distance, altitude ->
                                        XYChart.Data<Number, Number>(distance / 1000, altitude)
                                    }.toObservable()

                            when {
                                maxDistance > 30.0 -> {
                                    xAxis.tickUnit = 10.0
                                }
                            }

                            this.isLegendVisible = false
                            this.createSymbols = false
                            this.data.add(XYChart.Series("", data))

                            children.add(detailsWindow)

                        }
                        hbox(alignment = Pos.CENTER) {
                            button("Close") {
                                action {
                                    closeActivityDetailView()
                                }
                            }
                        }
                    }
                } else {
                    hbox(alignment = Pos.CENTER) {
                        button("Close") {
                            action {
                                closeActivityDetailView()
                            }
                        }
                    }
                }
            }
        }

        // init MapView-Cache
        // initOfflineCache()

        // set the custom css file for the MapView
        mapView.setCustomMapviewCssURL(javaClass.getResource("/mapview.css"))

        // finally, initialize the map view
        mapView.initialize(
            Configuration.builder()
                .showZoomControls(false)
                .build()
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
            marker.visible = true
            detailsWindow.children.add(yLine)
        }

        chartBackground.onMouseExited = EventHandler {
            detailsPopup.isVisible = false
            yLine.isVisible = false
            marker.visible = false
            detailsWindow.children.remove(yLine)
        }

        chartBackground.onMouseMoved = EventHandler { event: MouseEvent ->
            val x = event.x + chartBackground.layoutX
            val y = event.y + chartBackground.layoutY

            yLine.startX = x - 5
            yLine.endX = x - 5
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
            marker.position = newPosition
            // adding can only be done after coordinate is set
            mapView.addMarker(marker)
        }
    }

    private fun getMarkerPosition(displayPosition: Double): Coordinate? {
        val xValue: Number = areaChart.xAxis.getValueForDisplay(displayPosition)
        val index = findStreamDataIndex(xValue)
        if (index != null) {
            val latitudeLongitude = activity.stream?.latitudeLongitude?.data?.get(index)
            return Coordinate(latitudeLongitude?.get(0), latitudeLongitude?.get(1))
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
        if (track != null) {
            // add track
            mapView.addCoordinateLine(track)

            // get the extent for track
            val tracksExtent = Extent.forCoordinates(track.coordinateStream.toList())

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
        val start = activity.stream?.latitudeLongitude?.data?.first()
        val startPNGURL = javaClass.getResource("/images/startmarker.png")
        val startMarker = Marker(startPNGURL, -16, -16)
        startMarker.position = Coordinate(start?.get(0), start?.get(1))
        startMarker.visible = true

        mapView.addMarker(startMarker)
        markersCreatedOnClick[startMarker.id] = startMarker

        val end = activity.stream?.latitudeLongitude?.data?.last()

        val finishPNGURL = javaClass.getResource("/images/finishmarker.png")
        val endMarker = Marker(finishPNGURL, 0, -32)
        endMarker.position = Coordinate(end?.get(0), end?.get(1))
        endMarker.visible = true

        mapView.addMarker(endMarker)
        markersCreatedOnClick[endMarker.id] = endMarker

        marker = Marker
            .createProvided(Marker.Provided.BLUE)
            .setVisible(false)

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
                val timeValueLabel = Label(datetime.plusSeconds(abs(time)).format(timeFormatter))
                vbox.add(HBox(10.0, timeValueLabel))

                // Distance
                val distance = activity.stream?.distance?.data?.get(index)
                val distanceLabel = Label("Distance:")
                val distanceValueLabel = Label(String.format("%.1f km", distance?.div(1000)))
                distanceValueLabel.font = Font.font("Arial", FontWeight.EXTRA_BOLD, 15.0)
                vbox.add(HBox(10.0, distanceLabel, distanceValueLabel))

                // Distance
                val altitude = activity.stream?.altitude?.data?.get(index)
                val altitudeLabel = Label("Altitude:")
                val altitudeValueLabel = Label("$altitude m")
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
                setOnFinished { marker.position = newPosition }
            }

            override fun interpolate(v: Double) {
                val latitude = oldPosition.latitude + v * deltaLatitude
                val longitude = oldPosition.longitude + v * deltaLongitude
                marker.position = Coordinate(latitude, longitude)
            }
        }
        transition.play()
    }
}

