package me.nicolas.stravastats.business


import com.fasterxml.jackson.annotation.JsonProperty

data class Map<T, U>(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("resource_state")
    val resourceState: Int,
    @JsonProperty("summary_polyline")
    val summaryPolyline: String?
)