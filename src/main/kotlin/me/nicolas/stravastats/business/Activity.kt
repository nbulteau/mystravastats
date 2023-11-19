package me.nicolas.stravastats.business


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import me.nicolas.stravastats.utils.formatDate
import me.nicolas.stravastats.utils.formatSeconds
import kotlin.math.abs

const val Run = "Run"
const val Ride = "Ride"
const val VirtualRide = "VirtualRide"
const val InlineSkate = "InlineSkate"
const val Hike = "Hike"
const val Commute = "Commute"
const val AlpineSki = "AlpineSki"

@JsonIgnoreProperties(ignoreUnknown = true)
data class Activity(
    val athlete: AthleteRef,
    @JsonProperty("average_speed")
    val averageSpeed: Double,
    @JsonProperty("average_cadence")
    val averageCadence: Double,
    @JsonProperty("average_heartrate")
    val averageHeartrate: Double,
    @JsonProperty("max_heartrate")
    val maxHeartrate: Double,
    @JsonProperty("average_watts")
    val averageWatts: Double,
    val commute: Boolean,
    var distance: Double,
    @JsonProperty("device_watts")
    val deviceWatts: Boolean = false,
    @JsonProperty("elapsed_time")
    var elapsedTime: Int,
    @JsonProperty("elev_high")
    val elevHigh: Double,
    val id: Long,
    val kilojoules: Double,
    @JsonProperty("max_speed")
    val maxSpeed: Double,
    @JsonProperty("moving_time")
    val movingTime: Int,
    val name: String,
    @JsonProperty("start_date")
    val startDate: String,
    @JsonProperty("start_date_local")
    val startDateLocal: String,
    @JsonProperty("start_latlng")
    val startLatlng: List<Double>?,
    @JsonProperty("total_elevation_gain")
    val totalElevationGain: Double,
    val type: String,
    @JsonProperty("upload_id")
    val uploadId: Long,
    @JsonProperty("weighted_average_watts")
    val weightedAverageWatts: Int,
) {
    var stream: Stream? = null

    override fun toString() = "${name.trim()} (${startDateLocal.formatDate()})"

    fun getFormattedSpeed(): String {
        return if (type == "Run") {
            "${getSpeed()}/km"
        } else {
            "${getSpeed()} km/h"
        }
    }

    fun getSpeed(): String {
        return if (type == "Run") {
            (elapsedTime * 1000 / distance).formatSeconds()
        } else {
            "%.02f".format(distance / elapsedTime * 3600 / 1000)
        }
    }

    fun calculateTotalAscentGain(): Double {
        if (stream?.altitude?.data != null) {
            val deltas = stream?.altitude?.data?.zipWithNext { a, b -> b - a }
            return abs(deltas?.filter { it < 0 }?.sumOf { it }!!)
        }
        return 0.0
    }

    fun calculateTotalDescentGain(): Double {
        if (stream?.altitude?.data != null) {
            val deltas = stream?.altitude?.data?.zipWithNext { a, b -> b - a }
            return deltas?.filter { it > 0 }?.sumOf { it }!!
        }
        return 0.0
    }

    /**
     * Remove non-moving sections of the activity.
     */
    fun removeNonMoving() {

        if (stream == null) {
            return
        }

        var totDistance = 0.0
        var totSeconds = 0
        var totAltitude = 0.0

        val streamWithoutNonMovingData = Stream(
            Distance(mutableListOf(), 0, "high", "distance"),
            Time(mutableListOf(), 0, "high", "distance"),
            Moving(mutableListOf(), 0, "high", "distance"),
            Altitude(mutableListOf(), 0, "high", "distance"),
            LatitudeLongitude(mutableListOf(), 0, "high", "distance"),
            PowerStream(mutableListOf(), 0, "high", "distance")
        )
        streamWithoutNonMovingData.append(0.0, 0, 0.0)

        for (index in stream!!.distance.data.indices) {
            if (stream?.moving?.data?.get(index) == true) {
                val prevDist: Double = if (index == 0) 0.0 else stream?.distance?.data?.get(index - 1)!!
                val prevSeconds: Int = if (index == 0) 0 else stream?.time?.data?.get(index - 1)!!
                val prevAltitude: Double = if (index == 0) 0.0 else stream?.altitude?.data?.get(index - 1)!!

                totDistance += stream?.distance?.data?.get(index)!! - prevDist
                totSeconds += stream?.time?.data?.get(index)!! - prevSeconds
                totAltitude += stream?.altitude?.data?.get(index)!! - prevAltitude

                streamWithoutNonMovingData.append(totDistance, totSeconds, totAltitude)
            }
        }

        distance = totDistance
        elapsedTime = totSeconds

        stream = streamWithoutNonMovingData
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class AthleteRef(
    val id: Int,
)