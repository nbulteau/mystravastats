package me.nicolas.stravastats.business

import com.fasterxml.jackson.annotation.JsonProperty

data class Altitude(
    // The sequence of altitude values for this stream, in meters
    @JsonProperty("data")
    val `data`: MutableList<Double>,
    // The number of data points in this stream
    @JsonProperty("original_size")
    var originalSize: Int,
    @JsonProperty("resolution")
    val resolution: String,
    @JsonProperty("series_type")
    val seriesType: String
)