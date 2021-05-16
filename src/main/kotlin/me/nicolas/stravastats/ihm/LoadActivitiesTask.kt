package me.nicolas.stravastats.ihm

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import javafx.concurrent.Task
import me.nicolas.stravastats.MyStravaStatsProperties
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.service.StravaService
import me.nicolas.stravastats.strava.StravaApi
import java.time.LocalDate


internal interface LoadActivitiesTaskCompletionHandler {
    fun complete()
}

internal class LoadActivitiesTask(
    val clientId: String,
    private val clientSecret: String?
) : Task<Pair<Athlete?, List<Activity>>>() {

    private val stravaService = StravaService(StravaApi(loadPropertiesFromFile()))

    override fun call(): Pair<Athlete?, List<Activity>> {
        updateMessage("Waiting for your agreement to allow MyStravaStats to access to your Strava data ...")

        updateMessage("Loading athlete activities $clientId ...")
        val athlete = stravaService.getLoggedInAthlete(clientId, clientSecret)
        val activities = mutableListOf<Activity>()
        for (currentYear in LocalDate.now().year downTo 2010) {
            updateMessage("Loading $currentYear activities ...")
            activities.addAll(stravaService.loadActivitiesForAYear(clientId, clientSecret, currentYear))
        }

        updateMessage("All activities loaded.")

        return Pair(athlete, activities)
    }

    /**
     * Load properties from application.yml
     */
    private fun loadPropertiesFromFile(): MyStravaStatsProperties {
        val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
        mapper.registerModule(KotlinModule()) // Enable Kotlin support

        val inputStream = javaClass.getResourceAsStream("/application.yml")
        return mapper.readValue(inputStream, MyStravaStatsProperties::class.java)
    }
}

