package me.nicolas.stravastats.ihm.detailview

import javafx.scene.control.ToggleGroup
import javafx.scene.layout.VBox
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.SegmentEffort


class ActivityDetailView(activity: Activity, segmentEfforts: List<SegmentEffort>) : AbstractActivityDetailView(activity, segmentEfforts) {

    init {
        initMapView()
    }

    override fun VBox.addRadioButtons(toggleGroup: ToggleGroup) {
        // Nothing to do
    }
}

