package me.nicolas.stravastats.core

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.nicolas.stravastats.StravaStatsProperties
import me.nicolas.stravastats.infrastructure.StravaApi
import me.nicolas.stravastats.infrastructure.dao.Activity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime

internal class ActivityLoader(
    private val stravaStatsProperties: StravaStatsProperties,
    private val stravaApi: StravaApi
) {

    private val logger: Logger = LoggerFactory.getLogger(ActivityLoader::class.java)

    private val objectMapper = jacksonObjectMapper()

    fun getActivitiesFromFile(filePath: String): List<Activity> {

        logger.info("Get activities from file : $filePath")

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

        logger.info("Get activities with code : $authorizationCode")

        val token = stravaApi.getToken(clientId, clientSecret, authorizationCode)

        return getActivitiesWithAccessToken(clientId, year, token.accessToken)
    }

    fun getActivitiesWithAccessToken(clientId: String, year: Int, accessToken: String): List<Activity> {

        logger.info("Get activities with accessToken : $accessToken")

        val activities = stravaApi.getActivities(
            accessToken = accessToken,
            before = LocalDateTime.of(year, 12, 31, 23, 59),
            after = LocalDateTime.of(year, 1, 1, 0, 0)
        )

        if (stravaStatsProperties.saveActivitiesOnDisk) {
            // create a File object for the parent directory
            val activitiesDirectory = File("strava-$clientId-$year-${LocalDate.now()}")
            // have the object build the directory structure, if needed.
            activitiesDirectory.mkdirs()
            val writer: ObjectWriter = objectMapper.writer(DefaultPrettyPrinter())

            writer.writeValue(File(activitiesDirectory, "activities-$clientId-$year.json"), activities)
        }

        activities.forEach { activity ->
            val stream = stravaApi.getActivityStream(accessToken, activity)
            activity.stream = stream
        }

        if (stravaStatsProperties.saveActivitiesOnDisk) {
            // create a File object for the parent directory
            val activitiesDirectory = File("strava-$clientId-$year-${LocalDate.now()}")
            // have the object build the directory structure, if needed.
            activitiesDirectory.mkdirs()
            val writer: ObjectWriter = objectMapper.writer()

            writer.writeValue(File(activitiesDirectory, "activities-$clientId-$year-with-stream.json"), activities)
        }

        return activities
    }
}