package me.nicolas.stravastats.ihm

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.service.FitService
import java.time.LocalDate

internal class FitFilesLoadActivitiesTask(override val clientId: String) : LoadActivitiesTask(clientId) {

    private val fitService = FitService()

    override fun call(): Pair<Athlete?, List<Activity>> {
        updateMessage("Load FIT activities from fit-$clientId ...")
        val activities = mutableListOf<Activity>()
        for (currentYear in LocalDate.now().year downTo 2010) {
            updateMessage("Loading $currentYear activities ...")
            activities.addAll(fitService.loadActivitiesFromCache(clientId, currentYear))
        }
        updateMessage("All activities are loaded.")

        return Pair(null, activities)
    }
}