package me.nicolas.stravastats.service

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.business.Stream
import me.nicolas.stravastats.ihm.task.ProgressBarHelper.Companion.displayProgressBar
import me.nicolas.stravastats.service.ActivityHelper.Companion.filterActivities
import me.nicolas.stravastats.strava.StravaApi
import java.io.File
import java.nio.file.Files
import kotlin.io.path.name


internal class StravaService(private val clientId: String, clientSecret: String) {

    private val objectMapper = jacksonObjectMapper()

    private val stravaApi: StravaApi = StravaApi(clientId, clientSecret)

    private val prettyWriter: ObjectWriter = objectMapper.writer(DefaultPrettyPrinter())

    fun getLoggedInAthlete(): Athlete {

        print("\nLoad athlete description of clientId=$clientId... ")
        val athlete = stravaApi.getLoggedInAthlete()
        println("done")

        // Save into cache
        val activitiesDirectory = File("strava-$clientId")
        activitiesDirectory.mkdirs()
        prettyWriter.writeValue(File(activitiesDirectory, "athlete-$clientId.json"), athlete)

        return athlete
    }

    fun getActivities(year: Int): List<Activity> {

        print("Load activities of clientId=$clientId for year $year ... ")
        val activities = stravaApi.getActivities(year).filterActivities()
        println("done")

        // Save into cache
        val yearActivitiesDirectory = File("strava-$clientId", "strava-$clientId-$year")
        yearActivitiesDirectory.mkdirs()
        prettyWriter.writeValue(
            File(yearActivitiesDirectory, "activities-$clientId-$year.json"),
            activities
        )

        this.getActivitiesStreams(activities, yearActivitiesDirectory)

        return activities
    }

    private fun getActivitiesStreams(activities: List<Activity>, activitiesDirectory: File) {

        println("Load ${activities.size} activities streams ... ")

        var index = 0.0
        val writer: ObjectWriter = objectMapper.writer()

        // stream id files list
        val streamIdsSet = Files.walk(activitiesDirectory.toPath())
            .filter { Files.isRegularFile(it) }
            .filter { it.name.startsWith("stream-") }
            .map { it.name.substringAfter("stream-").toLong() }
            .toList().toSet()

        activities.forEach { activity ->
            displayProgressBar(++index / activities.size)

            // stream
            val streamFile = File(activitiesDirectory, "stream-${activity.id}")
            val stream: Stream?
            if (streamIdsSet.contains(activity.id)) {
                stream = objectMapper.readValue(streamFile, Stream::class.java)
            } else {
                stream = stravaApi.getActivityStream(activity)
                // save into cache
                if (stream != null) {
                    writer.writeValue(streamFile, stream)
                }
            }
            activity.stream = stream
        }

        println()
    }
}