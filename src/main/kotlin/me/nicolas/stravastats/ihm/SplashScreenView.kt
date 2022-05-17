package me.nicolas.stravastats.ihm

import javafx.collections.FXCollections
import javafx.concurrent.Worker
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.effect.DropShadow
import javafx.scene.layout.VBox
import me.nicolas.stravastats.ihm.task.FitFilesLoadActivitiesTask
import me.nicolas.stravastats.ihm.task.StravaCacheLoadActivitiesTask
import me.nicolas.stravastats.ihm.task.StravaLoadActivitiesTask
import tornadofx.*
import java.nio.file.Path

internal interface LoadActivitiesTaskCompletionHandler {
    fun complete()
}
internal class SplashScreenView : View("MyStravaStatistics") {

    companion object {
        const val SPLASH_WIDTH = 1024.0
    }

    private var loadProgress: ProgressBar
    private var progressText: Label

    override val root = VBox()

    init {
        val userData = this.primaryStage.userData as Map<*, *>
        val feedType: String = userData["type"] as String
        val clientId: String = userData["clientId"] as String

        val loadActivitiesTask = if (feedType == "FIT") {
            val cachePath = userData["path"] as Path
            FitFilesLoadActivitiesTask(cachePath)
        } else {
            val clientSecret: String? = userData["clientSecret"] as String?
            if (clientSecret != null) {
                StravaLoadActivitiesTask(clientId, clientSecret)
            } else {
                StravaCacheLoadActivitiesTask(clientId)
            }
        }

        val initCompletionHandler = object : LoadActivitiesTaskCompletionHandler {
            override fun complete() {
                try {
                    val (athlete, activities) = loadActivitiesTask.valueProperty().get()
                    this@SplashScreenView.replaceWith(
                        replacement = MainView(clientId, athlete, FXCollections.observableArrayList(activities)),
                        sizeToScene = true,
                        centerOnScreen = true
                    )
                } catch (exception: Exception) {
                    println(exception.message)
                }
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
