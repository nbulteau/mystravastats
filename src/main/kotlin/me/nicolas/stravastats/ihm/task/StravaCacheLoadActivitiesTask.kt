package me.nicolas.stravastats.ihm.task

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javafx.concurrent.Task
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.business.Stream
import me.nicolas.stravastats.ihm.task.ProgressBarHelper.Companion.displayProgressBar
import me.nicolas.stravastats.service.ActivityHelper.Companion.filterActivities
import java.io.File
import java.time.LocalDate

internal open class StravaCacheLoadActivitiesTask(private val clientId: String) :
    Task<Pair<Athlete?, List<Activity>>>() {

    private val objectMapper = jacksonObjectMapper()

    override fun call(): Pair<Athlete?, List<Activity>> {
        updateMessage("Load Strava activities from local cache ...")
        val athlete = getAthleteFromCache()

        val activities = mutableListOf<Activity>()
        for (currentYear in LocalDate.now().year downTo 2010) {
            updateMessage("Loading $currentYear activities ...")
            activities.addAll(loadActivitiesFromCache(currentYear))
        }

        updateMessage("All activities are loaded.")

        return Pair(athlete, activities)
    }

    private fun getAthleteFromCache(): Athlete? {
        var athlete: Athlete? = null

        val activitiesDirectory = File("strava-$clientId")
        val athleteJsonFile = File(activitiesDirectory, "athlete-$clientId.json")

        if (athleteJsonFile.exists()) {
            athlete = objectMapper.readValue(athleteJsonFile, Athlete::class.java)
        }

        return athlete
    }

    protected fun loadActivitiesFromCache(year: Int): List<Activity> {
        var activities = emptyList<Activity>()

        val yearActivitiesDirectory = File("strava-$clientId", "strava-$clientId-$year")
        val yearActivitiesJsonFile = File(yearActivitiesDirectory, "activities-$clientId-$year.json")

        if (yearActivitiesJsonFile.exists()) {
            print("\nLoad activities of clientId=$clientId from cache for year $year ... ")
            activities = objectMapper.readValue(yearActivitiesJsonFile, Array<Activity>::class.java)
                .toList()
                .filterActivities()
            println("done")

            // Load activities streams
            loadActivitiesStreams(activities, yearActivitiesDirectory)
        }

        return activities
    }

    private fun loadActivitiesStreams(activities: List<Activity>, activitiesDirectory: File) {
        var index = 0.0
        activities.forEach { activity ->
            displayProgressBar(++index / activities.size)

            val streamFile = File(activitiesDirectory, "stream-${activity.id}")
            if (streamFile.exists()) {
                activity.stream = objectMapper.readValue(streamFile, Stream::class.java)
            }
        }
        println()
    }
}