package me.nicolas.stravastats.infrastructure.dao

import com.fasterxml.jackson.annotation.JsonProperty

data class Distance(
    @JsonProperty("data")
    val `data`: MutableList<Double>,
    @JsonProperty("original_size")
    var originalSize: Int,
    @JsonProperty("resolution")
    val resolution: String,
    @JsonProperty("series_type")
    val seriesType: String
)