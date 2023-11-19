package me.nicolas.stravastats.strava

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.javalin.Javalin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.nicolas.stravastats.MyStravaStatsApp
import me.nicolas.stravastats.MyStravaStatsProperties
import me.nicolas.stravastats.business.*
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.eclipse.jetty.http.HttpStatus
import java.net.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.system.exitProcess


interface IStravaApi {
    fun retrieveLoggedInAthlete(): Optional<Athlete>
    fun getActivities(year: Int): List<Activity>
    fun getActivities(after: LocalDateTime): List<Activity>
    fun getActivityStream(activity: Activity): Optional<Stream>
    fun getActivity(activityId: Long): Optional<DetailledActivity>
}

internal class StravaApi(clientId: String, clientSecret: String) : IStravaApi {

    private val properties: MyStravaStatsProperties = loadPropertiesFromFile()

    private val objectMapper = jacksonObjectMapper()

    private val client: OkHttpClient = OkHttpClient.Builder().proxy(getupProxyFromEnvironment()).build()

    private var accessToken: String? = null
    private fun setAccessToken(accessToken: String) {
        this.accessToken = accessToken
    }

    init {
        setAccessToken(clientId, clientSecret)
    }

    override fun retrieveLoggedInAthlete(): Optional<Athlete> {
        try {
            return doGetLoggedInAthlete()
        } catch (connectException: ConnectException) {
            throw RuntimeException("Unable to connect to Strava API : ${connectException.message}")
        }
    }

    override fun getActivities(year: Int): List<Activity> {
        try {
            return doGetActivities(
                before = LocalDateTime.of(year, 12, 31, 23, 59),
                after = LocalDateTime.of(year, 1, 1, 0, 0)
            )
        } catch (connectException: ConnectException) {
            throw RuntimeException("Unable to connect to Strava API : ${connectException.message}")
        }
    }

    override fun getActivityStream(activity: Activity): Optional<Stream> {
        try {
            return doGetActivityStream(activity)
        } catch (connectException: ConnectException) {
            throw RuntimeException("Unable to connect to Strava API : ${connectException.message}")
        }
    }

    override fun getActivities(after: LocalDateTime): List<Activity> {
        try {
            return doGetActivities(after = after)
        } catch (connectException: ConnectException) {
            throw RuntimeException("Unable to connect to Strava API : ${connectException.message}")
        }
    }

    override fun getActivity(activityId: Long): Optional<DetailledActivity> {
        try {
            return doGetActivity(activityId)
        } catch (connectException: ConnectException) {
            throw RuntimeException("Unable to connect to Strava API : ${connectException.message}")
        }
    }

    private fun getupProxyFromEnvironment(): Proxy? {
        var httpsProxy = System.getenv()["https_proxy"]
        if (httpsProxy == null) {
            httpsProxy = System.getenv()["HTTPS_PROXY"]
        }
        if (httpsProxy != null) {
            try {
                val proxyUrl = URI(httpsProxy).toURL()
                println("Set http proxy : $proxyUrl")
                return Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyUrl.host, proxyUrl.port))
            } catch (malformedURLException: MalformedURLException) {
                println("Error configuring proxy : malformedURLException")
            }
        } else {
            println("No https proxy defined")
        }

        return null
    }

    private fun doGetLoggedInAthlete(): Optional<Athlete> {

        val url = "https://www.strava.com/api/v3/athlete"

        val request = Request.Builder()
            .url(url)
            .headers(buildRequestHeaders())
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                try {
                    val json = response.body?.string()
                    return if (json != null) {
                        Optional.of(objectMapper.readValue(json, Athlete::class.java))
                    } else {
                        Optional.empty()
                    }
                } catch (jsonMappingException: JsonMappingException) {
                    throw RuntimeException("Something was wrong with Strava API", jsonMappingException)
                }
            } else {
                throw RuntimeException("Something was wrong with Strava API for url $url : ${response.body}")
            }
        }
    }

    private fun doGetActivities(before: LocalDateTime? = null, after: LocalDateTime): List<Activity> {

        val activities = mutableListOf<Activity>()
        var page = 1
        var url = "https://www.strava.com/api/v3/athlete/activities?per_page=${properties.strava.pagesize}"
        if (before != null) {
            url += "&before=${before.atZone(ZoneId.of("Europe/Paris")).toEpochSecond()}"
        }
        url += "&after=${after.atZone(ZoneId.of("Europe/Paris")).toEpochSecond()}"

        val requestHeaders = buildRequestHeaders()
        do {
            val request = Request.Builder()
                .url("$url&page=${page++}")
                .headers(requestHeaders)
                .build()

            val result: List<Activity>
            client.newCall(request).execute().use { response ->
                if (response.code == 401) {
                    println("Invalid accessToken : $accessToken")
                    exitProcess(-1)
                }
                result = objectMapper.readValue(response.body?.string() ?: "")

                activities.addAll(result)
            }
        } while (result.isNotEmpty())

        return activities
    }

    private fun doGetActivityStream(activity: Activity): Optional<Stream> {

        // uploadId = 0 => this is a manual activity without streams
        if (activity.uploadId == 0L) {
            return Optional.empty()
        }
        val url = "https://www.strava.com/api/v3/activities/${activity.id}/streams" +
                "?keys=time,distance,latlng,altitude,moving,watts&key_by_type=true"

        val request: Request = Request.Builder()
            .url(url)
            .headers(buildRequestHeaders())
            .build()

        client.newCall(request).execute().use { response ->
            when {
                response.code >= HttpStatus.BAD_REQUEST_400 -> {
                    println("\nUnable to load streams for activity : ${activity.id}")
                    when (response.code) {
                        HttpStatus.TOO_MANY_REQUESTS_429 -> {
                            println(
                                "\nStrava API usage is limited on a per-application basis using both a 15-minute " +
                                        "and daily request limit." +
                                        "The default rate limit allows 100 requests every 15 minutes, " +
                                        "with up to 1,000 requests per day."
                            )
                            throw RuntimeException("Something was wrong with Strava API : 429 Too Many Requests")
                        }

                        else -> {
                            println("Something was wrong with Strava API for url ${response.request.url} : ${response.code} - ${response.body}")
                            return Optional.empty()
                        }
                    }
                }

                response.code == HttpStatus.OK_200 -> {
                    return try {
                        val json = response.body?.string()
                        return if (json != null) {
                            Optional.of(objectMapper.readValue(json, Stream::class.java))
                        } else {
                            Optional.empty()
                        }
                    } catch (jsonProcessingException: JsonProcessingException) {
                        println("\nUnable to load streams for activity : $activity")
                        Optional.empty()
                    }
                }

                else -> {
                    println("\nUnable to load streams for activity : $activity")
                    throw RuntimeException("Something was wrong with Strava API for url $url : ${response.code} - ${response.code}")
                }
            }
        }
    }

    private fun doGetActivity(activityId: Long): Optional<DetailledActivity> {
        val url = "https://www.strava.com/api/v3/activities/$activityId?include_all_efforts=true"

        val request: Request = Request.Builder()
            .url(url)
            .headers(buildRequestHeaders())
            .build()

        client.newCall(request).execute().use { response ->
            when {
                response.code >= HttpStatus.BAD_REQUEST_400 -> {
                    println("\nUnable to load activity : $activityId")
                    when (response.code) {
                        HttpStatus.TOO_MANY_REQUESTS_429 -> {
                            println(
                                "\nStrava API usage is limited on a per-application basis using both a 15-minute " +
                                        "and daily request limit." +
                                        "The default rate limit allows 100 requests every 15 minutes, " +
                                        "with up to 1,000 requests per day."
                            )
                            throw RuntimeException("Something was wrong with Strava API : 429 Too Many Requests")
                        }

                        else -> {
                            println("Something was wrong with Strava API for url ${response.request.url} : ${response.code} - ${response.body}")
                            return Optional.empty()
                        }
                    }
                }

                response.code == HttpStatus.OK_200 -> {
                    return try {
                        val json = response.body?.string()
                        return if (json != null) {
                            Optional.of(objectMapper.readValue(json, DetailledActivity::class.java))
                        } else {
                            Optional.empty()
                        }
                    } catch (jsonProcessingException: JsonProcessingException) {
                        println("\nUnable to load activity : $activityId - ${jsonProcessingException.message}")
                        Optional.empty()
                    }
                }

                else -> {
                    println("\nUnable to load activity : $activityId")
                    throw RuntimeException("Something was wrong with Strava API for url $url : ${response.code} - ${response.code}")
                }
            }
        }
    }

    private fun setAccessToken(clientId: String, clientSecret: String) {
        val url = "https://www.strava.com/api/v3/oauth/authorize" +
                "?client_id=$clientId" +
                "&response_type=code" +
                "&redirect_uri=http://localhost:8080/exchange_token" +
                "&approval_prompt=auto" +
                "&scope=read_all,activity:read_all,profile:read_all"
        MyStravaStatsApp.openBrowser(url)

        println()
        println("To grant MyStravaStats to read your Strava activities data: copy paste this URL in a browser")
        println(url)
        println()

        runBlocking {
            val channel = Channel<String>()

            // Start a web server
            val app = Javalin.create().start(8080)
            // GET /exchange_token to get code
            app.get("/exchange_token") { ctx ->
                val authorizationCode = ctx.req().getParameter("code")
                ctx.result("Access granted to read activities of clientId: $clientId.")

                launch {
                    // Get authorisation token with the code
                    val token = getToken(clientId, clientSecret, authorizationCode)
                    channel.send(token.accessToken)
                    // stop de web server
                    app.stop()
                }
            }

            print("Waiting for your agreement to allow MyStravaStats to access to your Strava data ...")
            val accessTokenFromToken = channel.receive()
            println(" access granted.")
            setAccessToken(accessTokenFromToken)
        }
    }

    private fun getToken(clientId: String, clientSecret: String, authorizationCode: String): Token {

        val url = "${properties.strava.url}/api/v3/oauth/token"

        val payload = mapOf(
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "code" to authorizationCode,
            "grant_type" to "authorization_code"
        )
        val body = objectMapper.writeValueAsString(payload).toRequestBody("application/json".toMediaType())
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            try {
                if (response.code == 200) {
                    return objectMapper.readValue(response.body?.string() ?: "", Token::class.java)
                } else {
                    throw RuntimeException("Something was wrong with Strava API for url $url : ${response.body}")
                }
            } catch (ex: Exception) {
                throw RuntimeException("Something was wrong with Strava API for url $url", ex)
            }
        }
    }

    private fun buildRequestHeaders() = Headers.Builder()
        .set("Accept", "application/json")
        .set("ContentType", "application/json")
        .set("Authorization", "Bearer $accessToken")
        .build()

    /**
     * Load properties from application.yml
     */
    private fun loadPropertiesFromFile(): MyStravaStatsProperties {
        val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
        mapper.registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        ) // Enable Kotlin support

        val inputStream = javaClass.getResourceAsStream("/application.yml")

        return mapper.readValue(inputStream, MyStravaStatsProperties::class.java)
    }
}
