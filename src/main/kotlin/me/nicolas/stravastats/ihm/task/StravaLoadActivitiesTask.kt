package me.nicolas.stravastats.ihm.task

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.service.StravaService
import me.nicolas.stravastats.strava.StravaApi
import java.time.LocalDate

internal class StravaLoadActivitiesTask(
    override val clientId: String,
    private val clientSecret: String
) : LoadActivitiesTask(clientId) {

    private val stravaService = StravaService(StravaApi(loadPropertiesFromFile()))

    override fun call(): Pair<Athlete?, List<Activity>> {
        updateMessage("Waiting for your agreement to allow MyStravaStats to access to your Strava data ...")
        val athlete = stravaService.getLoggedInAthlete(clientId, clientSecret)

        val activities = mutableListOf<Activity>()
        for (currentYear in LocalDate.now().year downTo 2010) {
            updateMessage("Loading $currentYear activities ...")
            activities.addAll(stravaService.loadActivitiesFromStrava(clientId, clientSecret, currentYear))
        }
        updateMessage("All activities are loaded.")

        return Pair(athlete, activities)
    }
}