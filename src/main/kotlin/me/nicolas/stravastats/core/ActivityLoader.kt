package me.nicolas.stravastats.core

import com.beust.jcommander.ParameterException
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

    private var accessToken: String? = null

    /**
     * Load activities
     */
    fun loadActivities(
        clientId: String,
        year: Int,
        accessToken: String?,
        clientSecret: String?,
        authorizationCode: String?
    ): List<Activity> {

        if (accessToken == null && this.accessToken == null && (authorizationCode != null && clientSecret != null)) {
            val token = stravaApi.getToken(clientId, clientSecret, authorizationCode)

            println("-accessToken ${token.accessToken}")

            this.accessToken = token.accessToken
        } else if (this.accessToken == null) {
            this.accessToken = accessToken
        }

        return when {
            // with access token
            this.accessToken != null -> getActivitiesWithAccessToken(clientId, year, this.accessToken!!)

            // from local cache
            authorizationCode == null && this.accessToken == null -> getActivitiesFromFile(clientId, year)

            else -> throw ParameterException("-code with -clientSecret or -accessToken must be provided")
        }
    }

    private fun getActivitiesFromFile(
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
                .filter { activity ->
                    activity.type == "Ride" ||
                            activity.type == "Run" ||
                            activity.type == "Hike" ||
                            activity.type == "InlineSkate"
                }
            println("done")

            // Load activities streams
            loadActivitiesStreams(activities, yearActivitiesDirectory)
        }

        return activities
    }

    private fun getActivitiesWithAccessToken(clientId: String, year: Int, accessToken: String): List<Activity> {

        // only get activities of type (Run, Bike and Hike)
        print("Load activities of clientId=$clientId for year $year ... ")
        val activities = stravaApi.getActivities(
            accessToken = accessToken,
            before = LocalDateTime.of(year, 12, 31, 23, 59),
            after = LocalDateTime.of(year, 1, 1, 0, 0)
        ).filter { activity ->
            activity.type == "Ride" ||
                    activity.type == "Run" ||
                    activity.type == "Hike" ||
                    activity.type == "InlineSkate"
        }
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
            displayProgressBar(++index / activities.size)

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
            displayProgressBar(++index / activities.size)

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