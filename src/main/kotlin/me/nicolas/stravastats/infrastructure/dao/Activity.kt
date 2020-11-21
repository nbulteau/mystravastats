package me.nicolas.stravastats.infrastructure.dao


import com.fasterxml.jackson.annotation.JsonProperty

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
    val distance: Double,
    @JsonProperty("elapsed_time")
    val elapsedTime: Int,
    @JsonProperty("elev_high")
    val elevHigh: Double,
    @JsonProperty("elev_low")
    val elevLow: Double,
    @JsonProperty("end_latlng")
    val endLatlng: List<Double>,
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
    val locationCountry: String,
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
    val startLatlng: List<Double>,
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
)

data class AthleteRef(
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("resource_state")
    val resourceState: Int
)