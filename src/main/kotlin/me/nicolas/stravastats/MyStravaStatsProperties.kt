package me.nicolas.stravastats

class MyStravaStatsProperties {

    val strava = Strava()

    class Strava {
        /**
         * Activities page size
         */
        var pagesize: Int = 100
        lateinit var url: String
    }

}