package me.nicolas.stravastats.infrastructure

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import khttp.get
import khttp.post
import me.nicolas.stravastats.infrastructure.dao.Activity
import me.nicolas.stravastats.infrastructure.dao.Stream
import me.nicolas.stravastats.infrastructure.dao.Token
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId


internal class StravaApi {
    companion object {
        const val stravaUrl = "https://www.strava.com"
    }

    private val logger: Logger = LoggerFactory.getLogger(StravaApi::class.java)

    private val mapper = jacksonObjectMapper()

    fun getActivities(accessToken: String, before: LocalDateTime, after: LocalDateTime): List<Activity> {

        logger.info("Get activities before : $before and after : $after")

        val activities = mutableListOf<Activity>()
        var page = 1
        var url = "$stravaUrl/api/v3/athlete/activities?per_page=100"
        url += "&before=${before.atZone(ZoneId.of("Europe/Paris")).toEpochSecond()}"
        url += "&after=${after.atZone(ZoneId.of("Europe/Paris")).toEpochSecond()}"

        val requestHeaders = buildRequestHeaders(accessToken)
        do {
            val response = get("$url&page=${page++}", requestHeaders)
            val result: List<Activity> = mapper.readValue(response.content)

            activities.addAll(result)
        } while (result.isNotEmpty())

        return activities
    }

    fun getActivityStream(accessToken: String, activity: Activity): Stream {

        logger.info("Get streams for ${activity.name}")

        val url = "$stravaUrl/api/v3/activities/${activity.id}/streams?keys=time,distance,moving&key_by_type=true"

        val requestHeaders = buildRequestHeaders(accessToken)
        val response = get(url, requestHeaders)
        if (response.statusCode == 200) {
            return mapper.readValue(response.content)
        } else {
            throw RuntimeException("Something was wrong with Strava API ${response.text}")
        }
    }

    fun getToken(clientId: String, clientSecret: String, authorizationCode: String): Token {

        logger.info("Get token")

        val url = "$stravaUrl/api/v3/oauth/token"

        val payload = mapOf(
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "code" to authorizationCode,
            "grant_type" to "authorization_code"
        )
        val response = post(url, data = payload)
        if (response.statusCode == 200) {
            return mapper.readValue(response.content, Token::class.java)
        } else {
            throw RuntimeException("Something was wrong with Strava API ${response.text}")
        }
    }

    private fun buildRequestHeaders(accessToken: String) = mapOf(
        "Accept" to "application/json",
        "ContentType" to "application/json",
        "Authorization" to "Bearer $accessToken"
    )
}
