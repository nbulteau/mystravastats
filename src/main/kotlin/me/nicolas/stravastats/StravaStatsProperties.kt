package me.nicolas.stravastats

class StravaStatsProperties {

    val strava = Strava()

    class Strava {
        var pagesize: Int = 100
    }
}