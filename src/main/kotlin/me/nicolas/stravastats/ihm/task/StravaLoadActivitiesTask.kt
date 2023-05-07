package me.nicolas.stravastats.ihm.task

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.service.Context
import me.nicolas.stravastats.service.IStravaService
import me.nicolas.stravastats.service.StravaService
import java.time.LocalDate
import kotlin.system.measureTimeMillis

internal class StravaLoadActivitiesTask(clientId: String, clientSecret: String, private val allYears: Boolean = true) :
    StravaCacheLoadActivitiesTask(clientId) {

    private val stravaService: IStravaService = StravaService.getInstance(Context(clientId, clientSecret))

    override fun call(): Pair<Athlete?, List<Activity>> {
        updateMessage("Waiting for your agreement to allow MyStravaStats to access to your Strava data ...")
        val athlete = stravaService.getLoggedInAthlete()

        val activities = mutableListOf<Activity>()
        val currentYear = LocalDate.now().year
        val elapsed = measureTimeMillis {
            if (allYears) {
                for (yearToLoad in currentYear downTo 2010) {
                    updateMessage("Loading $yearToLoad activities ...")
                    activities.addAll(stravaService.retrieveActivities(yearToLoad))
                }
            } else {
                updateMessage("Loading $currentYear activities ...")
                activities.addAll(stravaService.retrieveActivities(currentYear))
                for (yearToLoad in currentYear - 1 downTo 2010) {
                    updateMessage("Loading $yearToLoad activities ...")
                    activities.addAll(loadActivitiesFromCache(yearToLoad))
                }
            }
        }
        updateMessage("All activities are loaded.")
        println("All activities are loaded in ${elapsed / 1000} s.")

        return Pair(athlete, activities)
    }
}