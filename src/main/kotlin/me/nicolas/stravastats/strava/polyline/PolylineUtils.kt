package me.nicolas.stravastats.strava.polyline


object PolylineUtils {
    fun toString(polyline: List<Point>): String {
        var str = "[ "
        for (p in polyline) {
            str += p
        }
        return "$str ]"
    }

    fun toMarkers(polyline: List<Point>): String {
        var str = ""
        for (p in polyline) {
            str += "|" + p.lat + "," + p.lng
        }
        return str.substring(1, str.length)
    }
}