package me.nicolas.stravastats.business.gpx

internal const val GPX_NODE = "gpx"
internal const val WPT_NODE = "wpt"
internal const val TRK_NODE = "trk"
internal const val VERSION_ATTR = "version"
internal const val CREATOR_ATTR = "creator"
internal const val LAT_ATTR = "lat"
internal const val LON_ATTR = "lon"
internal const val ELE_NODE = "ele"
internal const val SPEED_NODE = "speed"
internal const val TIME_NODE = "time"
internal const val NAME_NODE = "name"
internal const val CMT_NODE = "cmt"
internal const val DESC_NODE = "desc"
internal const val SRC_NODE = "src"
internal const val MAGVAR_NODE = "magvar"
internal const val GEOIDHEIGHT_NODE = "geoidheight"
internal const val SYM_NODE = "sym"
internal const val TYPE_NODE = "type"
internal const val FIX_NODE = "fix"
internal const val SAT_NODE = "sat"
internal const val HDOP_NODE = "hdop"
internal const val VDOP_NODE = "vdop"
internal const val PDOP_NODE = "pdop"
internal const val AGEOFGPSDATA_NODE = "ageofdgpsdata"
internal const val DGPSID_NODE = "dgpsid"
internal const val NUMBER_NODE = "number"
internal const val TRKSEG_NODE = "trkseg"
internal const val TRKPT_NODE = "trkpt"
internal const val RTE_NODE = "rte"
internal const val RTEPT_NODE = "rtept"

data class Gpx(
    val version: String? = null,
    val creator: String? = null,
    val tracks: List<Track>? = null,
    val routes: List<Route>? = null,
    val wayPoints: List<WayPoint>? = null
)
