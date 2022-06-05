package me.nicolas.stravastats.ihm.task

import javafx.concurrent.Task
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.service.FitService
import java.nio.file.Path
import java.time.LocalDate

internal class FitFilesLoadActivitiesTask(
    private val cachePath: Path
) : Task<Pair<Athlete?, List<Activity>>>() {

    private val fitService = FitService(cachePath)

    override fun call(): Pair<Athlete?, List<Activity>> {
        updateMessage("Load FIT activities from $cachePath ...")
        val activities = mutableListOf<Activity>()
        for (currentYear in LocalDate.now().year downTo 2010) {
            updateMessage("Loading $currentYear activities ...")
            val loadedActivities = fitService.loadActivitiesFromCache(currentYear)
            activities.addAll(loadedActivities)
        }
        updateMessage("${activities.size} fit files loaded")

        return Pair(null, activities)
    }
}