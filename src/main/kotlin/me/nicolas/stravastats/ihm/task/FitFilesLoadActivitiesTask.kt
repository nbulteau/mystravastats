package me.nicolas.stravastats.ihm.task

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.service.FitCache
import java.nio.file.Path
import java.time.LocalDate

internal class FitFilesLoadActivitiesTask(
    override val clientId: String,
    private val cachePath: Path
) : LoadActivitiesTask(clientId) {

    private val fitCache = FitCache(cachePath)

    override fun call(): Pair<Athlete?, List<Activity>> {
        updateMessage("Load FIT activities from $cachePath ...")
        val activities = mutableListOf<Activity>()
        for (currentYear in LocalDate.now().year downTo 2010) {
            updateMessage("Loading $currentYear activities ...")
            val loadedActivities = fitCache.loadActivitiesFromCache(currentYear)
            activities.addAll(loadedActivities)
        }
        updateMessage("${activities.size} fit files loaded")

        return Pair(null, activities)
    }
}