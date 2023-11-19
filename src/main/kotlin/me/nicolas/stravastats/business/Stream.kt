package me.nicolas.stravastats.business

import com.fasterxml.jackson.annotation.JsonProperty

data class Stream(
    val distance: Distance,
    val time: Time,
    val moving: Moving?,
    val altitude: Altitude?,
    @JsonProperty("latlng")
    val latitudeLongitude: LatitudeLongitude?,
    val watts: PowerStream?,
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