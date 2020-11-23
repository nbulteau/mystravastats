package me.nicolas.stravastats

class StravaStatsProperties {

    val strava = Strava()

    class Strava {
        /**
         * Activities page size
         */
        var pagesize: Int = 100
    }

    /**
     * Set this flag to true to remove sections of activities where no movement was reported, such as when
     * standing still at a red light or stopping to look at a map.
     */
    val removingNonMovingSections = false

    /**
     * Set this flag to true to save activities on disk.
     */
    val saveActivitiesOnDisk = true

}