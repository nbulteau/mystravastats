package me.nicolas.stravastats.ihm.detailview

import com.sothawo.mapjfx.Coordinate
import com.sothawo.mapjfx.CoordinateLine
import javafx.scene.chart.XYChart
import javafx.scene.paint.Color
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.SegmentEffort
import me.nicolas.stravastats.service.statistics.calculateBestElevationForDistance
import tornadofx.toObservable

class ActivityWithGradientDetailView(
    activity: Activity,
    latitudeLongitudesList: List<List<Double>>,
    distancesList: List<Double>,
    altitudesList: List<Double>,
    powersList: List<Int>,
    segmentEfforts: List<SegmentEffort>
) : AbstractActivityDetailView(
    activity,
    latitudeLongitudesList,
    distancesList,
    altitudesList,
    powersList,
    segmentEfforts
) {

    init {
        statsTracks.addAll(buildElevationStatsTracks())
        initMapView()
    }

    private fun buildElevationStatsTracks(): List<StatsTrack> {
        return statsList.mapNotNull { distanceStat ->
            val activityEffort = activity.calculateBestElevationForDistance(distanceStat)
            if (activityEffort != null) {
                val coordinateLine =
                    CoordinateLine(latitudeLongitudesList.mapIndexedNotNull { index, coord ->
                        if (index >= activityEffort.idxStart && index <= activityEffort.idxEnd) {
                            Coordinate(coord[0], coord[1])
                        } else {
                            null
                        }
                    }).setColor(Color.BLUE).setWidth(5)
                val xyChartSeries = XYChart.Series(distancesList //.windowed(1, 10).flatten()
                    .zip(altitudesList) { distance, altitude ->
                        XYChart.Data<Number, Number>(distance / 1000, altitude)
                    }
                    .subList(activityEffort.idxStart, activityEffort.idxEnd)
                    .toObservable())

                StatsTrack(coordinateLine, xyChartSeries, activityEffort, StatType.GRADIENT)
            } else {
                null
            }
        }
    }
}