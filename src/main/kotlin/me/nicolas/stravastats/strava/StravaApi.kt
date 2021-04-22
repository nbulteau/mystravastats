package me.nicolas.stravastats.strava

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import khttp.get
import khttp.post
import me.nicolas.stravastats.MyStravaStatsProperties
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Stream
import me.nicolas.stravastats.business.Token
import org.eclipse.jetty.http.HttpStatus
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.system.exitProcess


internal class StravaApi(
    private val properties: MyStravaStatsProperties
) {

    private val mapper = jacksonObjectMapper()

    fun getActivities(accessToken: String, before: LocalDateTime, after: LocalDateTime): List<Activity> {

        val activities = mutableListOf<Activity>()
        var page = 1
        var url = "${properties.strava.url}/api/v3/athlete/activities?per_page=${properties.strava.pagesize}"
        url += "&before=${before.atZone(ZoneId.of("Europe/Paris")).toEpochSecond()}"
        url += "&after=${after.atZone(ZoneId.of("Europe/Paris")).toEpochSecond()}"

        val requestHeaders = buildRequestHeaders(accessToken)
        do {
            val response = get("$url&page=${page++}", requestHeaders)
            if (response.statusCode == 401) {
                println("Invalid accessToken : $accessToken")
                exitProcess(-1)
            }
            val result: List<Activity> = mapper.readValue(response.content)

            activities.addAll(result)
        } while (result.isNotEmpty())

        return activities
    }

    fun getActivityStream(accessToken: String, activity: Activity): Stream? {

        val url = "${properties.strava.url}/api/v3/activities/${activity.id}/streams" +
                "?keys=time,distance,altitude,moving&key_by_type=true"

        val requestHeaders = buildRequestHeaders(accessToken)
        val response = get(url, requestHeaders)

        return when {
            response.statusCode >= HttpStatus.BAD_REQUEST_400 -> {
                println("Unable to load streams for activity : $activity")
                if(response.statusCode == HttpStatus.TOO_MANY_REQUESTS_429) {
                    println("Strava API usage is limited on a per-application basis using both a 15-minute and daily request limit."
                        + "The default rate limit allows 100 requests every 15 minutes, with up to 1,000 requests per day.")
                    throw RuntimeException("Something was wrong with Strava API : 429 Too Many Requests")
                } else {
                    throw RuntimeException("Something was wrong with Strava API ${response.headers} - ${response.text}")
                }
            }
            response.statusCode == HttpStatus.OK_200 -> {
                return try {
                    mapper.readValue<Stream>(response.content)
                } catch (jsonProcessingException: JsonProcessingException) {
                    println("Unable to load streams for activity : $activity")
                    null
                }
            }
            else -> {
                println("Unable to load streams for activity : $activity")
                throw RuntimeException("Something was wrong with Strava API ${response.headers} - ${response.text}")
            }
        }
    }

    fun getToken(clientId: String, clientSecret: String, authorizationCode: String): Token {

        val url = "${properties.strava.url}/api/v3/oauth/token"

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
