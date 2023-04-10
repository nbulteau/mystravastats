package me.nicolas.stravastats.ihm.detailview

import javafx.scene.control.ToggleGroup
import javafx.scene.layout.VBox
import me.nicolas.stravastats.business.Activity


class ActivityDetailView(activity: Activity) : AbstractActivityDetailView(activity) {

    init {
        initMapView()
    }

    override fun VBox.addRadioButtons(toggleGroup: ToggleGroup) {
        // Nothing to do
    }
}

