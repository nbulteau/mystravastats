package me.nicolas.stravastats.core

import com.beust.jcommander.ParameterException
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.nicolas.stravastats.MyStravaStatsProperties
import me.nicolas.stravastats.strava.StravaApi
import java.io.File
import java.net.ConnectException
import java.time.LocalDateTime

import io.javalin.Javalin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.nicolas.stravastats.business.*
import java.awt.Desktop
import java.net.URI
import java.time.LocalDate


internal class ActivityService(
    private val myStravaStatsProperties: MyStravaStatsProperties,
    private val stravaApi: StravaApi
) {

    private val objectMapper = jacksonObjectMapper()

    private fun getActivitiesDirectoryName(clientId: String) = "strava-$clientId"

    private fun getYearActivitiesDirectoryName(clientId: String, year: Int) = "strava-$clientId-$year"

    private fun getYearActivitiesJsonFileName(clientId: String, year: Int) = "activities-$clientId-$year.json"

    private var accessToken: String? = null

    private fun setAccessToken(accessToken: String) {
        this.accessToken = accessToken
    }

    fun loadActivities(clientId: String, clientSecret: String?, year: Int?): List<Activity> {
        val activities = mutableListOf<Activity>()
        if (year != null) {
            activities.addAll(loadActivitiesForAYear(clientId, clientSecret, year))
        } else {
            for (currentYear in LocalDate.now().year downTo 2010) {
                activities.addAll(loadActivitiesForAYear(clientId, clientSecret, currentYear))
            }
        }
        return activities
    }

    private fun loadActivitiesForAYear(
        clientId: String,
        clientSecret: String?,
        year: Int
    ): List<Activity> {

        // get accessToken using client secret
        if (clientSecret != null && this.accessToken == null) {
            setAccessToken(clientId, clientSecret)
        }

        return if (this.accessToken == null) {
            getActivitiesFromCache(clientId, year)
        } else {
            try {
                getActivitiesFromStrava(clientId, year)
            } catch (connectException: ConnectException) {
                throw ParameterException("Unable to connect to Strava API : ${connectException.message}")
            }
        }
    }

    private fun setAccessToken(clientId: String, clientSecret: String) {
        println()
        println("To grant MyStravaStats to read your Strava activities data: copy paste this URL in a browser")
        val url =
            "http://www.strava.com/api/v3/oauth/authorize" +
                    "?client_id=${clientId}" +
                    "&response_type=code" +
                    "&redirect_uri=http://localhost:8080/exchange_token" +
                    "&approval_prompt=auto" +
                    "&scope=read_all,activity:read_all"
        println(url)
        Desktop.getDesktop().browse(URI(url))
        println()

        runBlocking {
            val channel = Channel<String>()

            // Start a web server
            val app = Javalin.create().start(8080)
            // GET /exchange_token to get code
            app.get("/exchange_token") { ctx ->
                val authorizationCode = ctx.req.getParameter("code")
                ctx.result("Access granted to read activities of clientId: $clientId.")

                launch {
                    // Get authorisation token with the code
                    val token = stravaApi.getToken(clientId, clientSecret, authorizationCode)
                    channel.send(token.accessToken)
                    // stop de web server
                    app.stop()
                }
            }

            println("Waiting for your agreement to allow MyStravaStats to access to your Strava data ...")
            val accessTokenFromToken = channel.receive()
            print(" access granted.")
            setAccessToken(accessTokenFromToken)
        }
    }

    private fun getActivitiesFromStrava(
        clientId: String,
        year: Int
    ): List<Activity> {
        val yearActivitiesDirectory = getYearActivitiesDirectory(clientId, year)
        val activities = loadActivitiesAndSaveIntoCache(clientId, year, this.accessToken!!, yearActivitiesDirectory)
        loadActivitiesStreamsAndSaveIntoCache(activities, yearActivitiesDirectory, this.accessToken!!)
        return activities
    }

    private fun getActivitiesFromCache(
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
            print("\nLoad activities of clientId=$clientId for year $year ... ")
            val objectMapper = ObjectMapper()
            activities = objectMapper.readValue(yearActivitiesJsonFile, Array<Activity>::class.java)
                .toList()
                .filterActivities()
            println("done")

            // Load activities streams
            loadActivitiesStreamsAndSaveIntoCache(activities, yearActivitiesDirectory)
        }

        return activities
    }

    private fun loadActivitiesAndSaveIntoCache(
        clientId: String,
        year: Int,
        accessToken: String,
        yearActivitiesDirectory: File
    ): List<Activity> {
        // only get activities of type (Run, Bike and Hike)
        print("Load activities of clientId=$clientId for year $year ... ")
        val activities = stravaApi.getActivities(
            accessToken = accessToken,
            before = LocalDateTime.of(year, 12, 31, 23, 59),
            after = LocalDateTime.of(year, 1, 1, 0, 0)
        ).filterActivities()
        println("done")

        val prettyWriter: ObjectWriter = objectMapper.writer(DefaultPrettyPrinter())
        prettyWriter.writeValue(
            File(yearActivitiesDirectory, getYearActivitiesJsonFileName(clientId, year)),
            activities
        )
        return activities
    }

    private fun getYearActivitiesDirectory(clientId: String, year: Int): File {
        val activitiesDirectoryName = getActivitiesDirectoryName(clientId)
        val yearActivitiesDirectoryName = getYearActivitiesDirectoryName(clientId, year)

        val yearActivitiesDirectory = File(activitiesDirectoryName, yearActivitiesDirectoryName)
        yearActivitiesDirectory.mkdirs()
        return yearActivitiesDirectory
    }

    private fun loadActivitiesStreamsAndSaveIntoCache(
        activities: List<Activity>,
        activitiesDirectory: File,
        accessToken: String
    ) {
        println("Load ${activities.size} activities streams ... ")

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

    private fun loadActivitiesStreamsAndSaveIntoCache(
        activities: List<Activity>,
        activitiesDirectory: File
    ) {
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

    private fun List<Activity>.filterActivities() = this.filter { activity ->
        activity.type == Ride || activity.type == Run || activity.type == Hike || activity.type == InlineSkate
    }

    private fun displayProgressBar(progressPercentage: Double) {
        val width = 100 // progress bar width in chars
        print("\r[")
        var i = 0
        while (i <= (progressPercentage * width).toInt()) {
            print(".")
            i++
        }
        while (i < width) {
            print(" ")
            i++
        }
        print("]")
    }
}