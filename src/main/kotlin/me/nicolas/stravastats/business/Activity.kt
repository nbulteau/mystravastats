package me.nicolas.stravastats.business


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Activity(
    @JsonProperty("achievement_count")
    val achievementCount: Int,
    @JsonProperty("athlete")
    val athlete: AthleteRef,
    @JsonProperty("athlete_count")
    val athleteCount: Int,
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
    @JsonProperty("comment_count")
    val commentCount: Int,
    @JsonProperty("commute")
    val commute: Boolean,
    @JsonProperty("device_watts")
    val deviceWatts: Boolean,
    @JsonProperty("display_hide_heartrate_option")
    val displayHideHeartrateOption: Boolean,
    @JsonProperty("distance")
    var distance: Double,
    @JsonProperty("elapsed_time")
    var elapsedTime: Int,
    @JsonProperty("elev_high")
    val elevHigh: Double,
    @JsonProperty("elev_low")
    val elevLow: Double,
    @JsonProperty("end_latlng")
    val endLatlng: List<Double>?,
    @JsonProperty("external_id")
    val externalId: String,
    @JsonProperty("flagged")
    val flagged: Boolean,
    @JsonProperty("from_accepted_tag")
    val fromAcceptedTag: Boolean,
    @JsonProperty("gear_id")
    val gearId: String?,
    @JsonProperty("has_heartrate")
    val hasHeartrate: Boolean,
    @JsonProperty("has_kudoed")
    val hasKudoed: Boolean,
    @JsonProperty("heartrate_opt_out")
    val heartrateOptOut: Boolean,
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("kilojoules")
    val kilojoules: Double,
    @JsonProperty("kudos_count")
    val kudosCount: Int,
    @JsonProperty("location_city")
    val locationCity: Any?,
    @JsonProperty("location_country")
    val locationCountry: String?,
    @JsonProperty("location_state")
    val locationState: Any?,
    @JsonProperty("manual")
    val manual: Boolean,
    @JsonProperty("map")
    val map: Map,
    @JsonProperty("max_speed")
    val maxSpeed: Double,
    @JsonProperty("moving_time")
    val movingTime: Int,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("photo_count")
    val photoCount: Int,
    @JsonProperty("pr_count")
    val prCount: Int,
    @JsonProperty("private")
    val `private`: Boolean,
    @JsonProperty("resource_state")
    val resourceState: Int,
    @JsonProperty("start_date")
    val startDate: String,
    @JsonProperty("start_date_local")
    val startDateLocal: String,
    @JsonProperty("start_latitude")
    val startLatitude: Double,
    @JsonProperty("start_latlng")
    val startLatlng: List<Double>?,
    @JsonProperty("start_longitude")
    val startLongitude: Double,
    @JsonProperty("timezone")
    val timezone: String,
    @JsonProperty("total_elevation_gain")
    val totalElevationGain: Double,
    @JsonProperty("total_photo_count")
    val totalPhotoCount: Int,
    @JsonProperty("trainer")
    val trainer: Boolean,
    @JsonProperty("type")
    val type: String,
    @JsonProperty("upload_id")
    val uploadId: Long,
    @JsonProperty("upload_id_str")
    val uploadIdStr: String,
    @JsonProperty("utc_offset")
    val utcOffset: Double,
    @JsonProperty("visibility")
    val visibility: String,
    @JsonProperty("workout_type")
    val workoutType: Int
) {

    var stream: Stream? = null

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
            Altitude(mutableListOf(), 0, "high", "distance")
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

    override fun toString() = " - $name (${startDateLocal.formatDate()})"

    fun getFormattedSpeed(): String {
        return if (type == "Run") {
            "${(elapsedTime * 1000 / distance).formatSeconds()}/km"
        } else {
            "%.02f km/h".format(distance / elapsedTime * 3600 / 1000)
        }
    }
}

data class AthleteRef(
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("resource_state")
    val resourceState: Int
)