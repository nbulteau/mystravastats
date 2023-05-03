package me.nicolas.stravastats.service

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.business.DetailledActivity
import me.nicolas.stravastats.business.Stream
import me.nicolas.stravastats.ihm.task.ProgressBarHelper.Companion.displayProgressBar
import me.nicolas.stravastats.service.ActivityHelper.Companion.filterActivities
import me.nicolas.stravastats.strava.IStravaApi
import me.nicolas.stravastats.strava.StravaApi
import me.nicolas.stravastats.utils.SingletonHolder
import java.io.File
import java.nio.file.Files
import java.util.*
import kotlin.io.path.name


interface IStravaService {
    fun getLoggedInAthlete(): Athlete
    fun getActivities(year: Int): List<Activity>
    fun getActivity(year: Int, activityId: Long): Optional<DetailledActivity>
}

data class Context(
    val clientId: String,
    val clientSecret: String
)

internal class StravaService private constructor(context: Context) : IStravaService {

    companion object : SingletonHolder<StravaService, Context>(::StravaService)

    private val clientId = context.clientId

    private val stravaApi: IStravaApi = StravaApi(context.clientId, context.clientSecret)

    private val objectMapper = jacksonObjectMapper()

    private val prettyWriter: ObjectWriter = objectMapper.writer(DefaultPrettyPrinter())

    private val writer: ObjectWriter = objectMapper.writer()


    override fun getLoggedInAthlete(): Athlete {

        print("\nLoad athlete description of clientId=$clientId... ")
        val athlete = stravaApi.getLoggedInAthlete()
        println("done")

        if (athlete.isPresent) {
            // Save into cache
            val activitiesDirectory = File("strava-$clientId")
            activitiesDirectory.mkdirs()
            prettyWriter.writeValue(File(activitiesDirectory, "athlete-$clientId.json"), athlete.get())
        }

        return athlete.get()
    }

    override fun getActivities(year: Int): List<Activity> {

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

    override fun getActivity(year: Int, activityId: Long): Optional<DetailledActivity> {
        val activitiesDirectory = File("strava-$clientId", "strava-$clientId-$year")

        // stream id files list
        val detailledActivityIdsSet = Files.walk(activitiesDirectory.toPath())
            .filter { Files.isRegularFile(it) }
            .filter { it.name.startsWith("activity-") }
            .map { it.name.substringAfter("activity-").toLong() }
            .toList().toSet()

        val detailledActivityFile = File(activitiesDirectory, "activity-${activityId}")
        val detailledActivity: Optional<DetailledActivity>
        if (detailledActivityIdsSet.contains(activityId)) {
            detailledActivity =
                Optional.of(objectMapper.readValue(detailledActivityFile, DetailledActivity::class.java))
        } else {
            val optionalDetailledActivity = stravaApi.getActivity(activityId)
            // save into cache
            if (optionalDetailledActivity.isPresent) {
                detailledActivity = optionalDetailledActivity
                writer.writeValue(detailledActivityFile, optionalDetailledActivity.get())
            } else {
                detailledActivity = Optional.empty()
            }
        }

        return detailledActivity
    }

    private fun getActivitiesStreams(activities: List<Activity>, activitiesDirectory: File) {

        println("Load ${activities.size} activities streams ... ")

        var index = 0.0

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
                val optionalStream = stravaApi.getActivityStream(activity)
                // save into cache
                if (optionalStream.isPresent) {
                    stream = optionalStream.get()
                    writer.writeValue(streamFile, stream)
                } else {
                    stream = null
                }
            }
            activity.stream = stream
        }

        println()
    }
}