package me.nicolas.stravastats.strava.polyline

import kotlin.math.abs


/**
 * Simple geographical point represented by a couple of doubles. Google's GeoPoint is a couple of micro-degrees
 * represented by integers.
 */
class Point(val lat: Double, val lng: Double) {

    companion object {

        /**
         * Utility method to export coordinates for use with GeoJSON. This standard requires longitude first. See
         * http://geojson.org/geojson-spec.html#positions
         */
        fun toGeoJSON(points: List<Point>): String {
            val buff = StringBuilder("[")
            val itr = points.iterator()
            while (itr.hasNext()) {
                buff.append(toGeoJSON(itr.next()))
                if (itr.hasNext()) {
                    buff.append(",")
                }
            }
            buff.append("]")
            return buff.toString()
        }

        fun toGeoJSON(point: Point): String {
            return "[" + point.lng + "," + point.lat + "]"
        }
    }

    /**
     * We consider that two point are equals if both latitude and longitude are "nearly" the same. With a precision of
     * 1e-3 degree
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Point) {
            return false
        }
        return if (abs(other.lat - lat) > 0.001) {
            false
        } else {
            abs(other.lng - lng) <= 0.001
        }
    }

    override fun hashCode(): Int {
        var hash = 5
        hash =
            37 * hash + (java.lang.Double.doubleToLongBits(lat) xor (java.lang.Double.doubleToLongBits(lat) ushr 32)).toInt()
        hash =
            37 * hash + (java.lang.Double.doubleToLongBits(lng) xor (java.lang.Double.doubleToLongBits(lng) ushr 32)).toInt()
        return hash
    }

    override fun toString(): String {
        return "($lat, $lng)"
    }
}