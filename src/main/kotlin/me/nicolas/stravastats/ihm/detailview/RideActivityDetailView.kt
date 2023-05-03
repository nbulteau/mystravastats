package me.nicolas.stravastats.ihm.detailview

import javafx.scene.control.ToggleGroup
import javafx.scene.layout.VBox
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.SegmentEffort
import me.nicolas.stravastats.service.statistics.calculateBestElevationForDistance
import me.nicolas.stravastats.utils.formatSeconds
import tornadofx.action
import tornadofx.radiobutton
import tornadofx.tooltip

class RideActivityDetailView(activity: Activity, segmentEfforts: List<SegmentEffort>) :
    AbstractActivityDetailView(activity, segmentEfforts) {

    private var bestElevationFor500m = activity.calculateBestElevationForDistance(500.0)
    private val bestElevationFor500mTrack = buildTrack(bestElevationFor500m)

    private var bestElevationFor1000m = activity.calculateBestElevationForDistance(1000.0)
    private val bestElevationFor1000mTrack = buildTrack(bestElevationFor1000m)

    private var bestElevationFor5000m = activity.calculateBestElevationForDistance(5000.0)
    private val bestElevationFor5000mTrack = buildTrack(bestElevationFor5000m)

    private var bestElevationFor10000m = activity.calculateBestElevationForDistance(10000.0)
    private val bestElevationFor10000mTrack = buildTrack(bestElevationFor10000m)

    init {
        initMapView()
    }

    override fun VBox.addRadioButtons(toggleGroup: ToggleGroup) {

        if (bestElevationFor500m != null) {
            radiobutton(
                "Best gradient for 500 m : ${bestElevationFor500m?.getFormattedGradient()}", toggleGroup
            ) {
                tooltip {
                    val speed = bestElevationFor500m?.getFormattedSpeed()
                    val distance = "500 m"
                    val elapsedTime = bestElevationFor500m?.seconds?.formatSeconds()
                    text = "Speed: $speed\nDistance: $distance km\nElapsed Time: $elapsedTime"
                }
                action {
                    showTrack(listOf(bestElevationFor500mTrack))
                }
            }
        }
        if (bestElevationFor1000m != null) {
            radiobutton(
                "Best gradient for 1000 m : ${bestElevationFor1000m?.getFormattedGradient()}", toggleGroup
            ) {
                tooltip {
                    val speed = bestElevationFor1000m?.getFormattedSpeed()
                    val distance = "1000 m"
                    val elapsedTime = bestElevationFor1000m?.seconds?.formatSeconds()
                    text = "Speed: $speed\nDistance: $distance km\nElapsed Time: $elapsedTime"
                }
                action {
                    showTrack(listOf(bestElevationFor1000mTrack))
                }
            }
        }
        if (bestElevationFor5000m != null) {
            radiobutton(
                "Best gradient for 5000 m : ${bestElevationFor5000m?.getFormattedGradient()}", toggleGroup
            ) {
                tooltip {
                    val speed = bestElevationFor5000m?.getFormattedSpeed()
                    val distance = "5000 m"
                    val elapsedTime = bestElevationFor5000m?.seconds?.formatSeconds()
                    text = "Speed: $speed\nDistance: $distance km\nElapsed Time: $elapsedTime"
                }
                action {
                    showTrack(listOf(bestElevationFor5000mTrack))
                }
            }
        }
        if (bestElevationFor10000m != null) {
            radiobutton(
                "Best gradient for 10 000 m : ${bestElevationFor10000m?.getFormattedGradient()}", toggleGroup
            ) {
                tooltip {
                    val speed = bestElevationFor10000m?.getFormattedSpeed()
                    val distance = "10 000 m"
                    val elapsedTime = bestElevationFor10000m?.seconds?.formatSeconds()
                    text = "Speed: $speed\nDistance: $distance km\nElapsed Time: $elapsedTime"
                }
                action {
                    showTrack(listOf(bestElevationFor10000mTrack))
                }
            }
        }
    }
}