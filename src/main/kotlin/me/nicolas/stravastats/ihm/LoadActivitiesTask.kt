package me.nicolas.stravastats.ihm

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import javafx.concurrent.Task
import me.nicolas.stravastats.MyStravaStatsApp
import me.nicolas.stravastats.MyStravaStatsProperties
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.service.StravaService
import me.nicolas.stravastats.strava.StravaApi
import java.time.LocalDate


internal interface LoadActivitiesTaskCompletionHandler {
    fun complete()
}

internal class LoadActivitiesTask : Task<Pair<Athlete?, List<Activity>>>() {

    private val stravaService = StravaService(StravaApi(loadPropertiesFromFile()))

    override fun call(): Pair<Athlete?, List<Activity>> {
        if ( MyStravaStatsApp.myStravaStatsParameters.year != null) {
            updateMessage("Waiting for your agreement to allow MyStravaStats to access to your Strava data ...")
        }

        updateMessage("Loading athlete activities ${MyStravaStatsApp.myStravaStatsParameters.clientId} ...")
        val athlete = stravaService.getLoggedInAthlete(
            MyStravaStatsApp.myStravaStatsParameters.clientId,
            MyStravaStatsApp.myStravaStatsParameters.clientSecret
        )
        val activities = mutableListOf<Activity>()
        if (MyStravaStatsApp.myStravaStatsParameters.year != null) {
            updateMessage("Loading ${MyStravaStatsApp.myStravaStatsParameters.year} activities ...")
            activities.addAll(
                stravaService.loadActivitiesForAYear(
                    MyStravaStatsApp.myStravaStatsParameters.clientId,
                    MyStravaStatsApp.myStravaStatsParameters.clientSecret,
                    MyStravaStatsApp.myStravaStatsParameters.year!!
                )
            )
        } else {
            for (currentYear in LocalDate.now().year downTo 2010) {
                updateMessage("Loading $currentYear activities ...")
                activities.addAll(
                    stravaService.loadActivitiesForAYear(
                        MyStravaStatsApp.myStravaStatsParameters.clientId,
                        MyStravaStatsApp.myStravaStatsParameters.clientSecret, currentYear
                    )
                )
            }
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

