package me.nicolas.stravastats.service


import com.garmin.fit.*
import me.nicolas.stravastats.business.*
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.utils.inDateTimeFormatter
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

internal class FitService(private val cachePath: Path) {

    private val fitDecoder = FitDecoder()

    private val srtmService = SRTMService(Path.of("srtm30m"))

    /**
     * Load all FIT activities from cache ([cachePath])
     * @param year Year to load
     * @return An activity list
     */
    fun loadActivitiesFromCache(year: Int): List<Activity> {
        val yearActivitiesDirectory = File(cachePath.toFile(), "$year")
        val fitFiles = yearActivitiesDirectory.listFiles { file ->
            file.extension.lowercase(Locale.getDefault()) == "fit"
        }
        val activities: List<Activity> = fitFiles?.mapNotNull { fitFile ->
            try {
                val fitMessages = fitDecoder.decode(fitFile.inputStream())
                this.convertToActivity(fitMessages)
            } catch (exception: Exception) {
                println(exception)
                null
            }
        }?.toList() ?: emptyList()

        return activities
    }

    /**
     * Convert a FIT activity to a Strava activity
     * @param fitMessages The fit file to convert
     */
    private fun convertToActivity(fitMessages: FitMessages): Activity {
        val sessionMesg = fitMessages.sessionMesgs.first()

        val stream: Stream = buildStream(fitMessages.recordMesgs)

        // Athlete
        val athlete = AthleteRef(0)
        // The activity's average speed, in meters per second
        val averageSpeed: Double = sessionMesg?.avgSpeed?.toDouble() ?: 0.0
        // The effort's average cadence
        val averageCadence: Double = sessionMesg?.avgCadence?.toDouble() ?: 0.0
        // The heart rate of the athlete during this effort
        val averageHeartrate: Double = sessionMesg?.avgHeartRate?.toDouble() ?: 0.0
        // The maximum heart rate of the athlete during this effort
        val maxHeartrate: Double = sessionMesg?.maxHeartRate?.toDouble() ?: 0.0
        //The average wattage of this effort
        val averageWatts: Double = sessionMesg?.avgPower?.toDouble() ?: 0.0 // TODO : Calculate ?
        // Whether this activity is a commute
        val commute = false
        // The activity's distance, in meters
        val distance: Double = sessionMesg?.totalDistance?.toDouble() ?: 0.0
        // The activity's elapsed time, in seconds
        val elapsedTime: Int = sessionMesg?.totalElapsedTime?.toInt() ?: 0
        // The activity's highest elevation, in meters
        val extractedElevHigh: Double = extractElevHigh(sessionMesg)
        val elevHigh: Double = if (extractedElevHigh == 0.0) {
            stream.altitude?.data?.maxOf { it }!!
        } else {
            extractedElevHigh
        }
        // The unique identifier of the activity
        val id: Long = 0
        // The total work done in kilojoules during this activity. Rides only
        val kilojoules = 0.0
        // The activity's max speed, in meters per second
        val maxSpeed: Double = sessionMesg?.maxSpeed?.toDouble() ?: 0.0
        // The activity's moving time, in seconds
        val movingTime: Int = sessionMesg?.timestamp?.timestamp?.minus(sessionMesg.startTime?.timestamp!!)?.toInt()!!
        // The time at which the activity was started.
        val startDate: String = extractDate(sessionMesg.startTime?.timestamp!!)
        // The time at which the activity was started in the local timezone.
        val startDateLocal: String = extractDateLocal(sessionMesg.startTime?.timestamp!!)
        // Activity name
        val name = "${extractActivityType(sessionMesg.sport!!)} - $startDateLocal"
        // Latitude /longitude of the start point
        val extractedStartLatLng = extractLatLng(sessionMesg.startPositionLat, sessionMesg.startPositionLong)
        val startLatlng: List<Double>? = extractedStartLatLng.ifEmpty {
            stream.latitudeLongitude?.data?.first()
        }
        // Total elevation gain
        val deltas = stream.altitude?.data?.zipWithNext { a, b -> b - a }
        val sum = deltas?.filter { it > 0 }?.sumOf { it }
        val totalElevationGain: Double = sessionMesg.totalAscent?.toDouble() ?: sum!!
        // Activity type (i.e. Ride, Run ...)
        val type: String = extractActivityType(sessionMesg.sport!!)

        val activity = Activity(
            athlete = athlete,
            averageSpeed = averageSpeed,
            averageCadence = averageCadence,
            averageHeartrate = averageHeartrate,
            maxHeartrate = maxHeartrate,
            averageWatts = averageWatts,
            commute = commute,
            distance = distance,
            elapsedTime = elapsedTime,
            elevHigh = elevHigh,
            id = id,
            kilojoules = kilojoules,
            maxSpeed = maxSpeed,
            movingTime = movingTime,
            name = name,
            startDate = startDate,
            startDateLocal = startDateLocal,
            startLatlng = startLatlng,
            totalElevationGain = totalElevationGain,
            type = type,
            uploadId = 0,
            weightedAverageWatts = sessionMesg.avgPower?.toInt() ?: 0,
        )

        activity.stream = stream

        return activity
    }

    /**
     * Build Strava Stream structure using the GPS records
     */
    private fun buildStream(recordMesgs: List<RecordMesg>): Stream {
        // distance
        val dataDistance = recordMesgs.map { recordMesg -> recordMesg.distance.toDouble() }
        val streamDistance = Distance(
            data = dataDistance.toMutableList(),
            originalSize = dataDistance.size,
            resolution = "high",
            seriesType = "distance"
        )

        //  time
        val startTime = recordMesgs.first().timestamp.timestamp
        val dataTime = recordMesgs.map { recordMesg ->
            (recordMesg.timestamp.timestamp - startTime).toInt()
        }
        val streamTime = Time(
            data = dataTime.toMutableList(),
            originalSize = dataTime.size,
            resolution = "high",
            seriesType = "distance"
        )

        // latitude/longitude
        val dataLatitude = recordMesgs.map { recordMesg ->
            if (recordMesg.positionLat == null) {
                0
            } else {
                recordMesg.positionLat
            }
        }.toMutableList()
        dataLatitude.fixCoordinate()

        val dataLongitude = recordMesgs.map { recordMesg ->
            if (recordMesg.positionLong == null) {
                0
            } else {
                recordMesg.positionLong
            }
        }.toMutableList()
        dataLongitude.fixCoordinate()

        val dataLatitudeLongitude = dataLatitude.zip(dataLongitude) { lat, long -> extractLatLng(lat, long) }
        val streamLatitudeLongitude = LatitudeLongitude(
            data = dataLatitudeLongitude,
            originalSize = dataLatitudeLongitude.size,
            resolution = "high",
            seriesType = "distance"
        )

        // altitude
        val dataAltitude = if (recordMesgs.first().altitude != null) {
            recordMesgs.map { recordMesg ->
                recordMesg.altitude.toDouble()
            }
        } else {
            smooth(generateDataAltitude(dataLatitudeLongitude))
        }
        val streamAltitude = Altitude(
            data = dataAltitude.toMutableList(),
            originalSize = dataAltitude.size,
            resolution = "high",
            seriesType = "distance"
        )

        // power
        val dataPower = recordMesgs.map { recordMesg ->
            recordMesg.power
        }
        val streamPower = PowerStream(
            data = dataPower.toMutableList(),
            originalSize = dataPower.size,
            resolution = "high",
            seriesType = "distance"
        )

        return Stream(streamDistance, streamTime, null, streamAltitude, streamLatitudeLongitude, streamPower)
    }

    private fun smooth(data: List<Double>, size: Int = 5): List<Double> {
        val smooth = DoubleArray(data.size)
        for (i in 0 until size) {
            smooth[i] = data[i]
        }
        for (i in size until data.size - size) {
            smooth[i] = data.subList(i - size, i + size).sum() / (2 * size + 1)
        }
        for (i in data.size - size until data.size) {
            smooth[i] = data[i]
        }

        return smooth.toList()
    }


    private fun generateDataAltitude(latitudeLongitudeList: List<List<Double>>): MutableList<Double> {
        return srtmService.getElevation(latitudeLongitudeList).toMutableList()
    }

    private fun extractLatLng(lat: Int?, lng: Int?): List<Double> {
        return if (lat != null && lng != null) {
            // 11930465 = (2^32 / 360)
            listOf(lat.toDouble() / 11930465, lng.toDouble() / 11930465)
        } else {
            emptyList()
        }
    }

    private fun extractActivityType(sport: Sport): String {
        return when (sport) {
            Sport.CYCLING -> "Ride"
            Sport.RUNNING -> "Run"
            Sport.INLINE_SKATING -> "InlineSkate"
            Sport.ALPINE_SKIING -> "AlpineSki"
            Sport.HIKING -> "Hike"
            else -> "Unknown"
        }
    }

    private fun extractDateLocal(value: Long): String {
        var localDateTime = LocalDateTime.of(1989, 12, 31, 0, 0, 0, 0)
        if (value >= 0L) {
            localDateTime = localDateTime.plusSeconds(value)
        }
        return localDateTime
            .atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(inDateTimeFormatter)
    }

    private fun extractDate(value: Long): String {
        var localDateTime = LocalDateTime.of(1989, 12, 31, 0, 0, 0, 0)
        if (value >= 0L) {
            localDateTime = localDateTime.plusSeconds(value)
        }

        return localDateTime.format(inDateTimeFormatter)
    }

    private fun extractElevHigh(sessionMesg: SessionMesg): Double {
        return if (sessionMesg.maxAltitude != null) {
            sessionMesg.maxAltitude.toDouble()
        } else if (sessionMesg.enhancedMaxAltitude != null) {
            sessionMesg.enhancedMaxAltitude.toDouble()
        } else {
            0.0
        }
    }

    /**
     * Fix missing coordinates
     */
    private fun MutableList<Int>.fixCoordinate() {
        var index = 0

        // if start with 0 : get the first valid value
        if (this.first() == 0) {
            val firstValidValue: Int = try {
                this.first { it != 0 }
            } catch (noSuchElementException: NoSuchElementException) {
                0
            }
            while (index < this.size && this[index] == 0) {
                this[index] = firstValidValue
                index++
            }
        }

        // if a value is missing set average value
        while (index < this.size) {
            if (this[index] == 0) {
                val lastValidValue: Int = this[index - 1]
                val firstValidValue: Int = try {
                    this.drop(index).first { it != 0 }
                } catch (noSuchElementException: NoSuchElementException) {
                    lastValidValue
                }
                while (this[index] == 0) {
                    this[index] = (lastValidValue + firstValidValue) / 2
                    index++
                }
            }
            index++
        }
    }
}


