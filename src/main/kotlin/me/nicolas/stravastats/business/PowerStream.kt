package me.nicolas.stravastats.business

import com.fasterxml.jackson.annotation.JsonProperty

data class PowerStream(
    @JsonProperty("data")
    val `data`: MutableList<Int>,
    @JsonProperty("original_size")
    val originalSize: Int,
    val resolution: String,
    @JsonProperty("series_type")
    val seriesType: String,
)