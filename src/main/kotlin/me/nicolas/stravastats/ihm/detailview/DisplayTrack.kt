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
    abstract val distance: Double
    abstract val elapsedTime: Int
    abstract val formattedSpeed: String
    abstract val formattedGradient: String

    open fun buildTooltipText(): String {
        val formattedDistance = "%.02f".format(this.distance / 1000)
        val formattedElapsedTime = elapsedTime.formatSeconds()
        return "Speed: $formattedSpeed\nDistance: $formattedDistance km\nElapsed time: $formattedElapsedTime\nAverage gradient: $formattedGradient"
    }
}

enum class StatType { GRADIENT, SPEED }
data class StatsTrack(
    override val track: CoordinateLine,
    override val altitudeAreaChart: XYChart.Series<Number, Number>,
    val activityEffort: ActivityEffort,
    val statType: StatType
) : DisplayTrack(track, altitudeAreaChart) {
    override val distance: Double
        get() = activityEffort.distance

    override val elapsedTime: Int
        get() = activityEffort.seconds

    override val formattedSpeed: String
        get() = activityEffort.getFormattedSpeed()

    override val formattedGradient: String
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
    override val formattedSpeed: String,
    override val formattedGradient: String
) : DisplayTrack(track, altitudeAreaChart) {
    override val distance: Double
        get() = segmentEffort.distance

    override val elapsedTime: Int
        get() = segmentEffort.elapsedTime

    override fun toString(): String {
        return "${segmentEffort.name} : $formattedSpeed"
    }

    override fun buildTooltipText(): String {
        val pr = if (this.segmentEffort.prRank != null) {
            "PR : ${this.segmentEffort.prRank} best time\n"
        } else {
            ""
        }
        return "${super.buildTooltipText()}\n$pr"
    }
}