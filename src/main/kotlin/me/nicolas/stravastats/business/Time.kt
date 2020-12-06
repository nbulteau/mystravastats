package me.nicolas.stravastats.business

import com.fasterxml.jackson.annotation.JsonProperty

data class Time(
    @JsonProperty("data")
    val `data`: MutableList<Int>,
    @JsonProperty("original_size")
    var originalSize: Int,
    @JsonProperty("resolution")
    val resolution: String,
    @JsonProperty("series_type")
    val seriesType: String
)