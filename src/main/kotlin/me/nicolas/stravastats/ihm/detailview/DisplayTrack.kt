package me.nicolas.stravastats.ihm.detailview

import com.sothawo.mapjfx.CoordinateLine
import javafx.scene.chart.XYChart
import me.nicolas.stravastats.business.ActivityEffort
import me.nicolas.stravastats.business.SegmentEffort
import me.nicolas.stravastats.utils.formatSeconds

sealed class DisplayTrack(
    open val track: CoordinateLine,
    open val altitudeAreaChart: XYChart.Series<Number, Number>,
) {
    abstract val distance: String
    abstract val elapsedTime: String
    abstract val formattedSpeed: String
}

enum class StatType { GRADIENT, SPEED }
data class StatsTrack(
    override val track: CoordinateLine,
    override val altitudeAreaChart: XYChart.Series<Number, Number>,
    val activityEffort: ActivityEffort,
    val statType: StatType
) : DisplayTrack(track, altitudeAreaChart) {
    override val distance: String
        get() = "%.02f".format(activityEffort.distance / 1000)

    override val elapsedTime: String
        get() = activityEffort.seconds.formatSeconds()

    override val formattedSpeed: String
        get() = activityEffort.getFormattedSpeed()

    val formattedGradient: String
        get() = activityEffort.getFormattedGradient()

    override fun toString(): String {
        return when (statType) {
            StatType.GRADIENT -> "Best gradient for ${activityEffort.distance} m : ${activityEffort.getFormattedGradient()}"
            StatType.SPEED -> "Best speed for ${activityEffort.distance} m : ${activityEffort.getFormattedSpeed()}"
        }
    }
}

data class SegmentTrack(
    override val track: CoordinateLine,
    override val altitudeAreaChart: XYChart.Series<Number, Number>,
    val segmentEffort: SegmentEffort,
    val activityType: String,
) : DisplayTrack(track, altitudeAreaChart) {
    override val distance: String
        get() = "%.02f".format(segmentEffort.distance / 1000)

    override val elapsedTime: String
        get() = segmentEffort.elapsedTime.formatSeconds()

    override val formattedSpeed: String
        get() = segmentEffort.getFormattedSpeed(activityType)

    override fun toString(): String {
        return "${segmentEffort.name} : ${segmentEffort.getFormattedSpeed(activityType)}"
    }
}