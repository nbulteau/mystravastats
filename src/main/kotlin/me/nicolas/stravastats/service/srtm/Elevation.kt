package me.nicolas.stravastats.service.srtm

/**
 *
 * The Elevation object contains elevation data along with the actual point data came from.
 *
 */
data class Elevation(val elevation: Double, val point: Point)