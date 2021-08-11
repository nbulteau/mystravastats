package me.nicolas.stravastats.business

import kotlin.collections.Map

abstract class Badge(
    open val name: String,
    open val elevation: Int,
    open val isCompleted: Boolean = false
)

data class LocationBadge(
    override val name: String,
    override val elevation: Int,
    override val isCompleted: Boolean = false,
    val geoCoordinate: GeoCoordinate,
    val location: String
) : Badge(name, elevation, isCompleted) {

    fun match(latitude: Double, longitude: Double) = geoCoordinate.haversineInM(latitude, longitude) < 150
}

data class DistanceBadge(
    override val name: String,
    override val elevation: Int,
    override val isCompleted: Boolean = false,
    val levels: Map<String, Double>
) : Badge(name, elevation, isCompleted)

data class ElevationBadge(
    override val name: String,
    override val elevation: Int,
    override val isCompleted: Boolean = false,
    val levels: Map<String, Double>
) : Badge(name, elevation, isCompleted)

data class TimeBadge(
    override val name: String,
    override val elevation: Int,
    override val isCompleted: Boolean = false,
    val levels: Map<String, Double>
) : Badge(name, elevation, isCompleted)

