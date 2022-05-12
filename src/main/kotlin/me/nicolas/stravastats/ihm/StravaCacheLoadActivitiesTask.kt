package me.nicolas.stravastats.ihm

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.service.StravaService
import me.nicolas.stravastats.strava.StravaApi
import java.time.LocalDate

internal open class StravaCacheLoadActivitiesTask(override val clientId: String) : LoadActivitiesTask(clientId) {

    private val stravaService = StravaService(StravaApi(this.loadPropertiesFromFile()))

    override fun call(): Pair<Athlete?, List<Activity>> {
        updateMessage("Load Strava activities from local cache ...")
        val athlete = stravaService.getAthleteFromCache(clientId)

        val activities = mutableListOf<Activity>()
        for (currentYear in LocalDate.now().year downTo 2010) {
            updateMessage("Loading $currentYear activities ...")
            activities.addAll(stravaService.loadActivitiesFromCache(clientId, currentYear))
        }
        updateMessage("All activities are loaded.")

        return Pair(athlete, activities)
    }
}