package me.nicolas.stravastats.business

import com.fasterxml.jackson.annotation.JsonProperty

data class Moving(
    @JsonProperty("data")
    val `data`: List<Boolean>,
    @JsonProperty("original_size")
    val originalSize: Int,
    @JsonProperty("resolution")
    val resolution: String,
    @JsonProperty("series_type")
    val seriesType: String
)