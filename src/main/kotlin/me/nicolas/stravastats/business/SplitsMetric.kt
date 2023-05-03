package me.nicolas.stravastats.business

import com.fasterxml.jackson.annotation.JsonProperty

data class SplitsMetric(
    @JsonProperty("average_speed")
    val averageSpeed: Double,
    @JsonProperty("average_grade_adjusted_speed")
    val averageGradeAdjustedSpeed: Double?,
    @JsonProperty("average_heartrate")
    val averageHeartRate: Double,
    val distance: Double,
    @JsonProperty("elapsed_time")
    val elapsedTime: Int,
    @JsonProperty("elevation_difference")
    val elevationDifference: Double,
    @JsonProperty("moving_time")
    val movingTime: Int,
    @JsonProperty("pace_zone")
    val paceZone: Int,
    val split: Int
)