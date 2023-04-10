package me.nicolas.stravastats.ihm.detailview

import com.sothawo.mapjfx.CoordinateLine
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.VBox
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.service.statistics.calculateBestElevationForDistance
import tornadofx.action
import tornadofx.radiobutton

class RideActivityDetailView(activity: Activity) : AbstractActivityDetailView(activity) {

    private var bestElevationFor500m = activity.calculateBestElevationForDistance(500.0)
    private val bestElevationFor500mTrack: CoordinateLine = buildTrack(bestElevationFor500m)

    private var bestElevationFor1000m = activity.calculateBestElevationForDistance(1000.0)
    private val bestElevationFor1000mTrack: CoordinateLine = buildTrack(bestElevationFor1000m)

    init {
        tracks.add(bestElevationFor500mTrack)
        tracks.add(bestElevationFor1000mTrack)

        initMapView()
    }

    override fun VBox.addRadioButtons(toggleGroup: ToggleGroup) {
        if (bestElevationFor500m != null) {
            radiobutton(
                "Best gradient for 500 m : ${bestElevationFor500m?.getFormattedGradient()}", toggleGroup
            ) {
                action {
                    tracks.forEach { track -> track.setVisible(false) }
                    bestElevationFor500mTrack.setVisible(true)
                }
            }
        }
        if (bestElevationFor1000m != null) {
            radiobutton(
                "Best gradient for 1000 m : ${bestElevationFor1000m?.getFormattedGradient()}", toggleGroup
            ) {
                action {
                    tracks.forEach { track -> track.setVisible(false) }
                    bestElevationFor1000mTrack.setVisible(true)
                }
            }
        }
    }
}