package me.nicolas.stravastats.ihm

import javafx.collections.FXCollections
import javafx.concurrent.Worker
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.effect.DropShadow
import javafx.scene.layout.VBox
import tornadofx.View
import tornadofx.label
import tornadofx.progressbar

internal class SplashScreenView : View("MyStravaStatistics") {

    companion object {
        const val SPLASH_WIDTH = 1024.0
    }

    private var loadProgress: ProgressBar
    private var progressText: Label

    override val root = VBox()

    init {
        val loadActivitiesTask = LoadActivitiesTask()

        val initCompletionHandler = object : LoadActivitiesTaskCompletionHandler {
            override fun complete() {
                val (athlete, activities) = loadActivitiesTask.valueProperty().get()
                this@SplashScreenView.replaceWith(
                    replacement = MainView(athlete, FXCollections.observableArrayList(activities)),
                    sizeToScene = true,
                    centerOnScreen = true
                )
            }
        }

        with(root) {
            loadProgress = progressbar {
                prefWidth = SPLASH_WIDTH
                progressProperty().bind(loadActivitiesTask.progressProperty())
            }
            progressText = label("Loading activities ...") {
                textProperty().bind(loadActivitiesTask.messageProperty())
            }
            style = "-fx-padding: 5; -fx-border-width: 5;"
            effect = DropShadow()
        }

        loadActivitiesTask.stateProperty().addListener { _, _, newState: Worker.State ->
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind()
                loadProgress.progress = 1.0

                initCompletionHandler.complete()
            }
        }
        Thread(loadActivitiesTask).start()
    }
}
