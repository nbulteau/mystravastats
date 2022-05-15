package me.nicolas.stravastats.ihm

import com.sothawo.mapjfx.*
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.geometry.Insets
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.service.formatSeconds
import me.nicolas.stravastats.service.formatSpeed
import tornadofx.*
import tornadofx.Stylesheet.Companion.fieldset
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


class ActivityDetailView(val activity: Activity) : View(activity.toString()) {

    override val root = borderpane {
        setPrefSize(800.0, 600.0)
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
                hbox {
                    label("Distance : %.02f km".format(activity.distance / 1000))
                    label("|")
                    label("Total elevation gain : %.0f m".format(activity.totalElevationGain))
                    label("|")
                    label("Moving time : ${activity.elapsedTime.formatSeconds()}")
                    label("|")
                    label("Average speed : ${activity.averageSpeed.formatSpeed("Ride")}")
                    children.style {
                        fontWeight = FontWeight.BOLD
                        font = Font.font("Verdana", 10.0)
                        padding = box(5.px)
                    }
                }
            }
            center = mapView
        }

        // init MapView-Cache
        val offlineCache = mapView.offlineCache
        val cacheDir = System.getProperty("java.io.tmpdir") + "/mapjfx-cache"
        // set the custom css file for the MapView
        try {
            Files.createDirectories(Paths.get(cacheDir))
            offlineCache.setCacheDirectory(cacheDir)
            // offlineCache.setActive(true)
        } catch (ioException: IOException) {
            // Nothing to do
        }

        // set the custom css file for the MapView
        mapView.setCustomMapviewCssURL(javaClass.getResource("/mapview.css"))
        // finally initialize the map view
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
        currentWindow?.setOnCloseRequest {
            it.consume()
            mapView.close()
            close()
        }
    }

    /**
     * finishes setup after the mpa is initialized
     */
    private fun afterMapIsInitialized() {
        // add track
        mapView.addCoordinateLine(track)

        // get the extent for track
        val tracksExtent = Extent.forCoordinates(track.coordinateStream.toList())
        val trackVisibleListener =
            ChangeListener { _: ObservableValue<out Boolean>, _: Boolean?, _: Boolean? ->
                mapView.setExtent(
                    tracksExtent
                )
            }
        track.visibleProperty().addListener(trackVisibleListener)

        // add start & end makers
        addMarkers()

        // Set the focus
        mapView.center = this.getTrackBounds().getCenter()

    }

    private fun addMarkers() {
        val start = activity.stream?.latitudeLongitude?.data?.first()
        val startMarker = Marker
            .createProvided(Marker.Provided.GREEN)
            .setPosition(Coordinate(start?.get(0) ?: 45.0, start?.get(1) ?: 8.0))
            .setVisible(true)
        mapView.addMarker(startMarker)

        val end = activity.stream?.latitudeLongitude?.data?.last()
        val endMarker = Marker
            .createProvided(Marker.Provided.RED)
            .setPosition(Coordinate(end?.get(0) ?: 45.0, end?.get(1) ?: 8.0))
            .setVisible(true)
        mapView.addMarker(endMarker)
        endMarker.visible = true
    }

    /**
     * @return the center of the bounds
     */
    private fun Pair<Coordinate, Coordinate>.getCenter(): Coordinate {
        val nw = this.first
        val se = this.second
        val lat: Double = (nw.latitude + se.latitude) * 0.5
        val lon: Double = (nw.longitude + se.longitude) * 0.5

        return Coordinate(lat, lon)
    }

    /**
     * Return the bounds of the track as a Pair of two coordinates:
     * upper left and lower right
     * @return The bounds
     */
    private fun getTrackBounds(): Pair<Coordinate, Coordinate> {
        val data = activity.stream?.latitudeLongitude?.data
        val upperBound = data?.maxOf { it[0] }
        val lowerBound = data?.minOf { it[0] }
        val leftBound = data?.minOf { it[1] }
        val rightBound = data?.maxOf { it[1] }

        return Pair(Coordinate(upperBound, leftBound), Coordinate(lowerBound, rightBound))
    }
}

