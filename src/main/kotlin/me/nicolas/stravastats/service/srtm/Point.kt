package me.nicolas.stravastats.service.srtm

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


/**
 * The Point object is the representation of a geodetic point on the surface of the earth
 */
data class Point(var latitude: Double = Double.NaN, var longitude: Double = Double.NaN) {

    companion object {
        /**
         * radius of the earth in km
         */
        private const val EARTH_RADIUS = 6371.0

        /**
         * number of arc seconds in 1 degree
         */
        const val ARCSECOND_IN_DEGREE = 3600.0
    }

    /**
     * add a lat/lon delta and get the new location back
     *
     * @param latitude in degrees
     * @param longitude in degrees
     * @return
     */
    fun add(latitude: Double, longitude: Double): Point {
        val newLat = this.latitude + latitude
        val newLon = this.longitude + longitude
        return Point(newLat, newLon)
    }

    /**
     * @param point
     * @return distance in meters
     */
    fun distance(point: Point): Double {
        val result = if (equals(point)) {
            0.0
        } else {
            val dLat = Math.toRadians(point.latitude - latitude)
            val dLon = Math.toRadians(point.longitude - longitude)
            val temp = sin(dLat / 2) * sin(dLat / 2)
            +cos(Math.toRadians(latitude)) * cos(Math.toRadians(point.latitude)) * sin(dLon / 2) * sin(dLon / 2)
            val a = 2 * atan2(sqrt(temp), sqrt(1 - temp))

            EARTH_RADIUS * a * 1000
        }
        return result
    }
}