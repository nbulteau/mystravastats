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
import tornadofx.View
import tornadofx.label
import tornadofx.progressbar
import java.nio.file.Path
import kotlin.system.exitProcess

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
            if (clientSecret == null) {
                StravaCacheLoadActivitiesTask(clientId)
            } else {
                val download: String = userData["download"] as String
                StravaLoadActivitiesTask(clientId, clientSecret, allYears = download == "all")
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
                    println("** Error ***")
                    exception.printStackTrace()
                    exitProcess(-1)
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
