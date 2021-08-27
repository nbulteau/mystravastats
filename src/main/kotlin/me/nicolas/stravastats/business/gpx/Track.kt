package me.nicolas.stravastats.business.gpx

data class Track(
    val trackPoints: List<WayPoint>,
    val name: String? = null,
    val comment: String? = null,
    val description: String? = null,
    val src: String? = null,
    val number: Int? = null,
    val type: String? = null
)
