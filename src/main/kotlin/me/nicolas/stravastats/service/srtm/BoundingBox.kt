package me.nicolas.stravastats.service.srtm


/**
 * The BoundingBox object defines an area on the earth in decimal degrees
 */
data class BoundingBox(
    val north: Double,
    val south: Double,
    val west: Double,
    val east: Double
) {

    /**
     * @param southWest
     * @param northEast
     */
    constructor(southWest: Point, northEast: Point) : this(
        northEast.latitude,
        southWest.latitude,
        southWest.longitude,
        northEast.longitude
    )

    /**
     * is the point in the box
     *
     * @param point
     * @return
     */
    operator fun contains(point: Point?): Boolean {
        var result = false
        if (point != null) {
            result = point.latitude in south..north && point.longitude >= west && point.longitude <= east
        }
        return result
    }
}