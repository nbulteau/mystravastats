package me.nicolas.stravastats.business.gpx

import java.util.*

data class WayPoint(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double? = null,
    val time: Date? = null,
    val speed: Float? = null,
    val magneticDeclination: Double? = null,
    val geoIdHeight: Double? = null,
    val name: String? = null,
    val comment: String? = null,
    val description: String? = null,
    val src: String? = null,
    val sym: String? = null,
    val type: String? = null,
    val fix: FixType? = null,
    val sat: Int? = null,
    val hDop: Double? = null,
    val vDop: Double? = null,
    val pDop: Double? = null,
    val ageOfGPSData: Double? = null,
    val dGpsId: Int? = null
)
