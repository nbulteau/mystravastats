package me.nicolas.stravastats.infrastructure.dao

import com.fasterxml.jackson.annotation.JsonProperty

data class Stream(
    @JsonProperty("distance")
    val distance: Distance,
    @JsonProperty("time")
    val time: Time,
    @JsonProperty("moving")
    val moving: Moving?,
    @JsonProperty("altitude")
    val altitude: Altitude?,
) {
    fun append(distance: Double, time: Int, altitude: Double) {
        this.distance.data.add(distance)
        this.distance.originalSize++
        this.time.data.add(time)
        this.time.originalSize++
        if (this.altitude != null) {
            this.altitude.data.add(altitude)
            this.altitude.originalSize++
        }
    }
}