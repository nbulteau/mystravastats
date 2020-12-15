package me.nicolas.stravastats.core

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.nicolas.stravastats.MyStravaStatsProperties
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Stream
import me.nicolas.stravastats.helpers.displayProgressBar
import me.nicolas.stravastats.strava.StravaApi
import java.io.File
import java.time.LocalDateTime

internal class ActivityLoader(
    private val myStravaStatsProperties: MyStravaStatsProperties,
    private val stravaApi: StravaApi
) {

    private val objectMapper = jacksonObjectMapper()

    private fun getActivitiesDirectoryName(clientId: String) = "strava-$clientId"

    private fun getYearActivitiesDirectoryName(clientId: String, year: Int) = "strava-$clientId-$year"

    private fun getYearActivitiesJsonFileName(clientId: String, year: Int) = "activities-$clientId-$year.json"


    fun getActivitiesFromFile(
        clientId: String,
        year: Int
    ): List<Activity> {

        val activitiesDirectoryName = getActivitiesDirectoryName(clientId)
        val yearActivitiesDirectoryName = getYearActivitiesDirectoryName(clientId, year)
        val yearActivitiesJsonFileName = getYearActivitiesJsonFileName(clientId, year)

        val yearActivitiesDirectory = File(activitiesDirectoryName, yearActivitiesDirectoryName)
        val yearActivitiesJsonFile = File(yearActivitiesDirectory, yearActivitiesJsonFileName)

        var activities = emptyList<Activity>()

        if (yearActivitiesJsonFile.exists()) {
            print("Load activities of clientId=$clientId for year $year ... ")
            val objectMapper = ObjectMapper()
            activities = objectMapper.readValue(yearActivitiesJsonFile, Array<Activity>::class.java)
                .toList()
                .filter { activity -> activity.type == "Ride" || activity.type == "Run" || activity.type == "Hike" }
            println("done")

            // Load activities streams
            loadActivitiesStreams(activities, yearActivitiesDirectory)
        }

        return activities
    }

    fun getActivitiesWithAuthorizationCode(
        clientId: String,
        year: Int,
        clientSecret: String,
        authorizationCode: String
    ): List<Activity> {

        val token = stravaApi.getToken(clientId, clientSecret, authorizationCode)

        println("-accessToken ${token.accessToken}")

        return getActivitiesWithAccessToken(clientId, year, token.accessToken)
    }

    fun getActivitiesWithAccessToken(clientId: String, year: Int, accessToken: String): List<Activity> {

        // only get activities of type (Run, Bike and Hike)
        print("Load activities of clientId=$clientId for year $year ... ")
        val activities = stravaApi.getActivities(
            accessToken = accessToken,
            before = LocalDateTime.of(year, 12, 31, 23, 59),
            after = LocalDateTime.of(year, 1, 1, 0, 0)
        ).filter { activity -> activity.type == "Ride" || activity.type == "Run" || activity.type == "Hike" }
        println("done")

        println("Load ${activities.size} activities streams ... ")
        if (myStravaStatsProperties.saveActivitiesOnDisk) {
            val activitiesDirectoryName = getActivitiesDirectoryName(clientId)
            val yearActivitiesDirectoryName = getYearActivitiesDirectoryName(clientId, year)

            val yearActivitiesDirectory = File(activitiesDirectoryName, yearActivitiesDirectoryName)
            yearActivitiesDirectory.mkdirs()

            val prettyWriter: ObjectWriter = objectMapper.writer(DefaultPrettyPrinter())
            prettyWriter.writeValue(
                File(yearActivitiesDirectory, getYearActivitiesJsonFileName(clientId, year)),
                activities
            )

            // Load activities streams
            loadActivitiesStreams(activities, yearActivitiesDirectory, accessToken)
        } else {
            // Load all activities streams
            activities.forEach { activity ->
                activity.stream = stravaApi.getActivityStream(accessToken, activity)
            }
        }

        return activities
    }

    private fun loadActivitiesStreams(
        activities: List<Activity>,
        activitiesDirectory: File,
        accessToken: String
    ) {
        var index = 0.0
        val writer: ObjectWriter = objectMapper.writer()
        activities.forEach { activity ->
            displayProgressBar(index++ / (activities.size))

            val streamFile = File(activitiesDirectory, "stream-${activity.id}")
            val stream: Stream?
            if (streamFile.exists()) {
                stream = objectMapper.readValue(streamFile, Stream::class.java)
            } else {
                stream = stravaApi.getActivityStream(accessToken, activity)
                if (stream != null) {
                    writer.writeValue(File(activitiesDirectory, "stream-${activity.id}"), stream)
                }
            }
            activity.stream = stream
        }
        println()
    }

    private fun loadActivitiesStreams(
        activities: List<Activity>,
        activitiesDirectory: File
    ) {
        var index = 0.0
        activities.forEach { activity ->
            displayProgressBar(index++ / (activities.size))

            val streamFile = File(activitiesDirectory, "stream-${activity.id}")
            val stream: Stream?
            if (streamFile.exists()) {
                stream = objectMapper.readValue(streamFile, Stream::class.java)
                activity.stream = stream
            }
        }
        println()
    }
}