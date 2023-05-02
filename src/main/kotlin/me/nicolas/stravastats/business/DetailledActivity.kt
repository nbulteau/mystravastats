package me.nicolas.stravastats.business

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class DetailledActivity(
    @JsonProperty("achievement_count")
    val achievementCount: Int,
    val athlete: MetaActivity,
    @JsonProperty("athlete_count")
    val athleteCount: Int,
    @JsonProperty("average_cadence")
    val averageCadence: Double,
    @JsonProperty("average_speed")
    val averageSpeed: Double,
    @JsonProperty("average_temp")
    val averageTemp: Int,
    @JsonProperty("average_watts")
    val averageWatts: Double,
    val calories: Double,
    @JsonProperty("comment_count")
    val commentCount: Int,
    val commute: Boolean,
    val description: String,
    @JsonProperty("device_name")
    val deviceName: String,
    @JsonProperty("device_watts")
    val deviceWatts: Boolean,
    val distance: Int,
    @JsonProperty("elapsed_time")
    val elapsedTime: Int,
    @JsonProperty("elev_high")
    val elevHigh: Double,
    @JsonProperty("elev_low")
    val elevLow: Double,
    @JsonProperty("embed_token")
    val embedToken: String,
    @JsonProperty("end_latlng")
    val endLatLng: List<Double>,
    @JsonProperty("external_id")
    val externalId: String,
    val flagged: Boolean,
    @JsonProperty("from_accepted_tag")
    val fromAcceptedTag: Boolean,
    val gear: Gear,
    @JsonProperty("gear_id")
    val gearId: String,
    @JsonProperty("has_heartrate")
    val hasHeartRate: Boolean,
    @JsonProperty("has_kudoed")
    val hasKudoed: Boolean,
    @JsonProperty("hide_from_home")
    val hideFromHome: Boolean,
    val id: Long,
    val kilojoules: Double,
    @JsonProperty("kudos_count")
    val kudosCount: Int,
    @JsonProperty("leaderboard_opt_out")
    val leaderboardOptOut: Boolean,
    @JsonProperty("map")
    val map: GeoMap?,
    val manual: Boolean,
    @JsonProperty("max_speed")
    val maxSpeed: Double,
    @JsonProperty("max_watts")
    val maxWatts: Int,
    @JsonProperty("moving_time")
    val movingTime: Int,
    val name: String,
    @JsonProperty("pr_count")
    val prCount: Int,
    @JsonProperty("private")
    val isPrivate: Boolean,
    @JsonProperty("resource_state")
    val resourceState: Int,
    @JsonProperty("segment_efforts")
    val segmentEfforts: List<SegmentEffort>,
    @JsonProperty("segment_leaderboard_opt_out")
    val segmentLeaderboardOptOut: Boolean,
    @JsonProperty("splits_metric")
    val splitsMetric: List<SplitsMetric>,
    @JsonProperty("sport_type")
    val sportType: String,
    @JsonProperty("start_date")
    val startDate: String,
    @JsonProperty("start_date_local")
    val startSateLocal: String,
    @JsonProperty("start_latlng")
    val startLatLng: List<Double>,
    @JsonProperty("suffer_score")
    val sufferScore: Double?,
    val timezone: String,
    @JsonProperty("total_elevation_gain")
    val totalElevationGain: Int,
    @JsonProperty("total_photo_count")
    val totalPhotoCount: Int,
    val trainer: Boolean,
    val type: String,
    @JsonProperty("upload_id")
    val uploadId: Long,
    @JsonProperty("utc_offset")
    val utcOffset: Int,
    @JsonProperty("weighted_average_watts")
    val weightedAverageWatts: Int,
    @JsonProperty("workout_type")
    val workoutType: Int
)