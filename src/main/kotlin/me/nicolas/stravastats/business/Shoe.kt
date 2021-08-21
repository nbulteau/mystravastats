package me.nicolas.stravastats.business

import com.fasterxml.jackson.annotation.JsonProperty

data class Shoe(
    @JsonProperty("distance")
    val distance: Int,
    @JsonProperty("id")
    val id: String,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("nickname")
    val nickname: String?,
    @JsonProperty("retired")
    val retired: Boolean?,
    @JsonProperty("converted_distance")
    val convertedDistance: Double,
    @JsonProperty("primary")
    val primary: Boolean,
    @JsonProperty("resource_state")
    val resourceState: Int
)