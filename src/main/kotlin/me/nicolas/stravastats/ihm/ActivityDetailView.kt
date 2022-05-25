package me.nicolas.stravastats.ihm

import com.sothawo.mapjfx.*
import com.sothawo.mapjfx.event.MapViewEvent
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.service.formatSeconds
import me.nicolas.stravastats.service.formatSpeed
import tornadofx.*
import kotlin.math.max
import kotlin.math.round


class ActivityDetailView(val activity: Activity) : View(activity.toString()) {

    private val markersCreatedOnClick = mutableMapOf<String, Marker>()

    override val root = borderpane {
        setPrefSize(1024.0, 768.0)
    }

    private val track: CoordinateLine =
        CoordinateLine(activity.stream?.latitudeLongitude?.data?.map { Coordinate(it[0], it[1]) })
            .setColor(Color.MAGENTA)
            .setVisible(true)

    private val mapView = MapView()

    init {
        FX.primaryStage.isResizable = true

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
                if (stream?.altitude != null) {
                    val minAltitude = max(round(((stream.altitude.data.minOf { it } - 20) / 10)) * 10, 0.0)
                    val maxAltitude = round(((stream.altitude.data.maxOf { it } + 10) / 10)) * 10
                    val maxDistance = stream.distance.data.maxOf { it } / 1000

                    vbox {
                        val xAxis = NumberAxis(0.0, maxDistance, 1.0)
                        val yAxis = NumberAxis(minAltitude, maxAltitude, 10.0)
                        areachart("Altitude", xAxis, yAxis) {
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
                        }
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
        // add track
        mapView.addCoordinateLine(track)

        // get the extent for track
        val tracksExtent = Extent.forCoordinates(track.coordinateStream.toList())

        // add start & end makers
        addMarkers()

        // set  bounds and fix zoom
        mapView.setExtent(tracksExtent)
        //mapView.zoom -= 1.0

        // Set the focus
        mapView.center = tracksExtent.getCenter()
    }

    private fun addMarkers() {
        mapView.addEventHandler(MapViewEvent.MAP_CLICKED) { event ->
            event.consume()
            if (markersCreatedOnClick.isNotEmpty()) {
                markersCreatedOnClick.values.forEach { marker ->
                    mapView.removeMarker(marker)
                }
                markersCreatedOnClick.clear()
            } else {
                val start = activity.stream?.latitudeLongitude?.data?.first()
                val startMarker = Marker
                    .createProvided(Marker.Provided.GREEN)
                    .setPosition(Coordinate(start?.get(0) ?: 45.0, start?.get(1) ?: 8.0))
                    .setVisible(true)
                mapView.addMarker(startMarker)
                markersCreatedOnClick[startMarker.id] = startMarker

                val end = activity.stream?.latitudeLongitude?.data?.last()
                val endMarker = Marker
                    .createProvided(Marker.Provided.RED)
                    .setPosition(Coordinate(end?.get(0) ?: 45.0, end?.get(1) ?: 8.0))
                    .setVisible(true)
                mapView.addMarker(endMarker)
                markersCreatedOnClick[endMarker.id] = endMarker
            }
        }
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
}

