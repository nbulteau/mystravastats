package me.nicolas.stravastats.business

import kotlin.math.*

/**
 * GeoCoordinate object is a geographic location determined by the latitude and longitude coordinates.
 */
data class GeoCoordinate(val latitude: Double, val longitude: Double) {

    companion object {
        private const val equatorialEarthRadius = 6378.1370
        private const val d2r = Math.PI / 180.0
    }

    /**
     * Calculate the distance (in meters) between two points on Earth using their latitude and longitude.
     */
    fun haversineInM(lat2: Double, long2: Double) = (1000.0 * haversineInKM(lat2, long2)).toInt()

    /**
     * Calculate the distance (in kilometers) between two points on Earth using their latitude and longitude.
     */
    fun haversineInKM(lat2: Double, long2: Double): Double {
        val long = (long2 - longitude) * d2r
        val lat = (lat2 - latitude) * d2r
        val a = sin(lat / 2.0).pow(2.0) + (cos(latitude * d2r) * cos(lat2 * d2r) * sin(long / 2.0).pow(2.0))
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))

        return equatorialEarthRadius * c
    }
}

