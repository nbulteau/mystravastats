package me.nicolas.stravastats.ihm.detailview

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.SegmentEffort


class ActivityDetailView(
    activity: Activity,
    latitudeLongitudesList: List<List<Double>>,
    distancesList: List<Double>,
    altitudesList: List<Double>,
    segmentEfforts: List<SegmentEffort>
) : AbstractActivityDetailView(activity, latitudeLongitudesList, distancesList, altitudesList, segmentEfforts) {

    init {
        initMapView()
    }
}

