package me.nicolas.stravastats.business.badges

import me.nicolas.stravastats.business.GeoCoordinate


data class FamousClimb(
    val name: String,
    val topOfTheAscent: Int,
    val geoCoordinate: GeoCoordinate,
    val alternatives: List<Alternative> = listOf()
)

data class Alternative(
    val name: String,
    val geoCoordinate: GeoCoordinate,
    val length: Double,
    val totalAscent: Int,
    val difficulty: Int,
    val averageGradient: Double
)
