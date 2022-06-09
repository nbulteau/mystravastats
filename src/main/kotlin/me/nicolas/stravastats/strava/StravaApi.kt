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
import me.nicolas.stravastats.MyStravaStatsProperties
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.business.Stream
import me.nicolas.stravastats.business.Token
import me.nicolas.stravastats.utils.openBrowser
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.eclipse.jetty.http.HttpStatus
import java.net.*
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.system.exitProcess


internal class StravaApi(clientId: String, clientSecret: String) {

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

    private fun getupProxyFromEnvironment(): Proxy? {
        var httpsProxy = System.getenv()["https_proxy"]
        if (httpsProxy == null) {
            httpsProxy = System.getenv()["HTTPS_PROXY"]
        }
        if (httpsProxy != null) {
            try {
                val proxyUrl = URL(httpsProxy)
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

    fun getLoggedInAthlete(): Athlete {
        try {
            return doGetLoggedInAthlete()
        } catch (connectException: ConnectException) {
            throw RuntimeException("Unable to connect to Strava API : ${connectException.message}")
        }
    }

    private fun doGetLoggedInAthlete(): Athlete {

        val url = "${properties.strava.url}/api/v3/athlete"

        val request = Request.Builder()
            .url(url)
            .headers(buildRequestHeaders())
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                try {
                    return objectMapper.readValue(response.body?.string() ?: "", Athlete::class.java)
                } catch (jsonMappingException: JsonMappingException) {
                    throw RuntimeException("Something was wrong with Strava API", jsonMappingException)
                }
            } else {
                throw RuntimeException("Something was wrong with Strava API for url $url : ${response.body}")
            }
        }
    }

    fun getActivities(year: Int): List<Activity> {
        try {
            return doGetActivities(
                before = LocalDateTime.of(year, 12, 31, 23, 59),
                after = LocalDateTime.of(year, 1, 1, 0, 0)
            )
        } catch (connectException: ConnectException) {
            throw RuntimeException("Unable to connect to Strava API : ${connectException.message}")
        }
    }

    private fun doGetActivities(before: LocalDateTime, after: LocalDateTime): List<Activity> {

        val activities = mutableListOf<Activity>()
        var page = 1
        var url = "${properties.strava.url}/api/v3/athlete/activities?per_page=${properties.strava.pagesize}"
        url += "&before=${before.atZone(ZoneId.of("Europe/Paris")).toEpochSecond()}"
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

    fun getActivityStream(activity: Activity): Stream? {
        try {
            return doGetActivityStream(activity)
        } catch (connectException: ConnectException) {
            throw RuntimeException("Unable to connect to Strava API : ${connectException.message}")
        }
    }

    private fun doGetActivityStream(activity: Activity): Stream? {

        // uploadId = 0 => this is a manual activity without streams
        if (activity.uploadId == 0L) {
            return null
        }
        val url = "${properties.strava.url}/api/v3/activities/${activity.id}/streams" +
                "?keys=time,distance,latlng,altitude,moving&key_by_type=true"

        val request: Request = Request.Builder()
            .url(url)
            .headers(buildRequestHeaders())
            .build()

        client.newCall(request).execute().use { response ->
            return when {
                response.code >= HttpStatus.BAD_REQUEST_400 -> {
                    println("\nUnable to load streams for activity : $activity")
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
                            println("Something was wrong with Strava API for url $url : ${response.code} - ${response.body}")
                            null
                        }
                    }
                }
                response.code == HttpStatus.OK_200 -> {
                    return try {
                        response.body?.let { objectMapper.readValue<Stream>(it.string()) }
                    } catch (jsonProcessingException: JsonProcessingException) {
                        println("\nUnable to load streams for activity : $activity")
                        null
                    }
                }
                else -> {
                    println("\nUnable to load streams for activity : $activity")
                    throw RuntimeException("Something was wrong with Strava API for url $url : ${response.code} - ${response.code}")
                }
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
                    "&scope=read_all,activity:read_all,profile:read_all"
        println(url)
        openBrowser(url)
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
