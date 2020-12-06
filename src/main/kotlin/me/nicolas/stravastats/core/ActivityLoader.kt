package me.nicolas.stravastats.core

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.nicolas.stravastats.MyStravaStatsProperties
import me.nicolas.stravastats.core.business.updateProgress
import me.nicolas.stravastats.infrastructure.StravaApi
import me.nicolas.stravastats.infrastructure.dao.Activity
import me.nicolas.stravastats.infrastructure.dao.Stream
import java.io.File
import java.nio.file.Paths
import java.time.LocalDateTime

internal class ActivityLoader(
    private val myStravaStatsProperties: MyStravaStatsProperties,
    private val stravaApi: StravaApi
) {

    private val objectMapper = jacksonObjectMapper()

    fun getActivitiesFromFile(filePath: String): List<Activity> {

        val objectMapper = ObjectMapper()
        val activities: Array<Activity> =
            objectMapper.readValue(
                Paths.get(filePath).toFile(),
                Array<Activity>::class.java
            )

        return activities.toList()
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
        )
        println("done")

        println("Load ${activities.size} activities streams ... ")
        var index = 0.0
        if (myStravaStatsProperties.saveActivitiesOnDisk) {
            val activitiesDirectoryName = "strava-$clientId"
            // create a File object for the parent directory
            val yearActivitiesDirectoryName = "strava-$clientId-$year"
            // create a File object for the parent directory
            val activitiesDirectory = File(activitiesDirectoryName, yearActivitiesDirectoryName)
            // have the object build the directory structure, if needed.
            activitiesDirectory.mkdirs()

            val prettyWriter: ObjectWriter = objectMapper.writer(DefaultPrettyPrinter())
            val writer: ObjectWriter = objectMapper.writer()

            prettyWriter.writeValue(File(activitiesDirectory, "activities-$clientId-$year.json"), activities)

            // Load activities streams
            activities.forEach { activity ->
                updateProgress(index++ / (activities.size))

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
            writer.writeValue(File(activitiesDirectory, "activities-$clientId-$year-with-stream.json"), activities)
        } else {
            // Load all activities streams
            activities.forEach { activity ->
                activity.stream = stravaApi.getActivityStream(accessToken, activity)
            }
        }
        println()

        return activities
    }
}