package me.nicolas.stravastats.business

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Gear(
    val distance: Long,
    val id: String,
    @JsonProperty("converted_distance")
    val convertedDistance: Double,
    val name: String,
    val nickname: String,
    val primary: Boolean,
    val retired: Boolean
)