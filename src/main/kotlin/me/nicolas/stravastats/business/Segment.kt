package me.nicolas.stravastats.business

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Segment(
    @JsonProperty("activity_type")
    val activityType: String,
    @JsonProperty("average_grade")
    val averageGrade: Double,
    val city: String?,
    @JsonProperty("climb_category")
    val climbCategory: Int,
    val country: String?,
    val distance: Double,
    @JsonProperty("elevation_high")
    val elevationHigh: Double,
    @JsonProperty("elevation_low")
    val elevationLow: Double,
    @JsonProperty("end_latlng")
    val endLatLng: List<Double>,
    val hazardous: Boolean,
    val id: Long,
    @JsonProperty("maximum_grade")
    val maximumGrade: Double,
    val name: String,
    @JsonProperty("private")
    val isPrivate: Boolean,
    @JsonProperty("resource_state")
    val resourceState: Int,
    val starred: Boolean,
    @JsonProperty("start_latlng")
    val startLatLng: List<Double>,
    val state: String?
)