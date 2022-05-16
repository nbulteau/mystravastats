package me.nicolas.stravastats.ihm

import javafx.collections.FXCollections
import javafx.concurrent.Worker
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.effect.DropShadow
import javafx.scene.layout.VBox
import me.nicolas.stravastats.ihm.task.FitFilesLoadActivitiesTask
import me.nicolas.stravastats.ihm.task.LoadActivitiesTaskCompletionHandler
import me.nicolas.stravastats.ihm.task.StravaCacheLoadActivitiesTask
import me.nicolas.stravastats.ihm.task.StravaLoadActivitiesTask
import tornadofx.*

internal class SplashScreenView(clientId: String, clientSecret: String?) : View("MyStravaStatistics") {

    companion object {
        const val SPLASH_WIDTH = 1024.0
    }

    constructor() : this("xxxxx", null)

    private var loadProgress: ProgressBar
    private var progressText: Label

    override val root = VBox()

    init {
        val feedType: String = this.primaryStage.userData as String

        val loadActivitiesTask = if (feedType == "FIT") {
            FitFilesLoadActivitiesTask(clientId)
        } else {
            if (clientSecret != null) {
                StravaLoadActivitiesTask(clientId, clientSecret)
            } else {
                StravaCacheLoadActivitiesTask(clientId)
            }
        }

        val initCompletionHandler = object : LoadActivitiesTaskCompletionHandler {
            override fun complete() {
                val (athlete, activities) = loadActivitiesTask.valueProperty().get()
                this@SplashScreenView.replaceWith(
                    replacement = MainView(clientId, athlete, FXCollections.observableArrayList(activities)),
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
