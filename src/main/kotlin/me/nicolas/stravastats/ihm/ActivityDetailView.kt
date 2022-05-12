package me.nicolas.stravastats.ihm

import com.sothawo.mapjfx.*
import javafx.beans.value.ObservableValue
import javafx.scene.paint.Color
import me.nicolas.stravastats.business.Activity
import tornadofx.FX
import tornadofx.View
import tornadofx.borderpane


class ActivityDetailView(val activity: Activity) : View(activity.toString()) {

    /** default zoom value.  */
    private val ZOOM_DEFAULT = 14.0

    override val root = borderpane {}

    private val track: CoordinateLine =
        CoordinateLine(activity.stream?.latitudeLongitude?.data?.map { Coordinate(it[0], it[1]) })
            .setColor(Color.MAGENTA)
            .setVisible(true)

    private val mapView = MapView()

    init {
        FX.primaryStage.isResizable = true

        with(root) {
            center = mapView
        }

        mapView.mapType = MapType.OSM
        // finally initialize the map view
        mapView.initialize(
            Configuration.builder()
                .showZoomControls(false)
                .build()
        )
        // watch the MapView's initialized property to finish initialization
        mapView.initializedProperty()
            .addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
                if (newValue) {
                    afterMapIsInitialized()
                }
            }
    }

    /**
     * finishes setup after the mpa is initialized
     */
    private fun afterMapIsInitialized() {
        mapView.zoom = ZOOM_DEFAULT
        val start = activity.stream?.latitudeLongitude?.data?.get(0)
        mapView.center = Coordinate(start?.get(0) ?: 45.0, start?.get(1) ?: 8.0)
        mapView.addCoordinateLine(track)
    }
}