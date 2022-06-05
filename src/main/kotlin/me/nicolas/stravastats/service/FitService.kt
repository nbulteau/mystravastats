package me.nicolas.stravastats.service


import com.garmin.fit.FitDecoder
import com.garmin.fit.RecordMesg
import com.garmin.fit.SessionMesg
import com.garmin.fit.Sport
import me.nicolas.stravastats.business.*
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

internal class FitService(private val cachePath: Path) {

    private val fitDecoder = FitDecoder()

    private val srtmService = SRTMService(Path.of("srtm30m"))

    fun loadActivitiesFromCache(year: Int): List<Activity> {
        val yearActivitiesDirectory = File(cachePath.toFile(), "$year")
        val fitFiles =
            yearActivitiesDirectory.listFiles { file -> file.extension.lowercase(Locale.getDefault()) == "fit" }

        val activities: List<Activity> = try {
            fitFiles?.map { file ->
                this.convertToActivity(file)
            }?.toList() ?: emptyList()
        } catch (exception: Exception) {
            println(exception)
            emptyList()
        }

        return activities
    }

    private fun convertToActivity(fitFile: File): Activity {
        val fitMessages = fitDecoder.decode(fitFile.inputStream())
        val sessionMesg = fitMessages.sessionMesgs[0]
        val recordMesgs = fitMessages.recordMesgs

        val stream: Stream = buildStream(recordMesgs)

        //
        val athlete = AthleteRef(0, 1)
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
        // The number of comments for this activity
        val commentCount = 0
        // Whether this activity is a commute
        val commute = false
        // Whether the watts are from a power meter, false if estimated
        val deviceWatts = false
        // ??
        val displayHideHeartrateOption = true
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
        // The activity's lowest elevation, in meters
        val extractedElevLow: Double = extractElevLow(sessionMesg)
        val elevLow: Double = if (extractedElevLow == 0.0) {
            stream.altitude?.data?.maxOf { it }!!
        } else {
            extractedElevLow
        }
        // An instance of LatLng (= List<Double>).
        val extractedEndLatLng = extractLatLng(sessionMesg.startPositionLat, sessionMesg.startPositionLong)
        val endLatlng: List<Double>? = extractedEndLatLng.ifEmpty {
            stream.latitudeLongitude?.data?.last()
        }
        // The identifier provided at upload time
        val externalId = "garmin_push_${fitFile.name.replace(".FIT", "")}"
        // The unique identifier of the activity
        val id: Long = 0
        // The total work done in kilojoules during this activity. Rides only
        val kilojoules = 0.0
        //
        val locationCity: Any? = null
        //
        val locationCountry: String? = null
        //
        val locationState: Any? = null
        // Whether this activity was created manually
        val manual = false
        // The activity's max speed, in meters per second
        val maxSpeed: Double = sessionMesg?.maxSpeed?.toDouble() ?: 0.0
        // The activity's moving time, in seconds
        val movingTime: Int = sessionMesg?.timestamp?.timestamp?.minus(sessionMesg.startTime?.timestamp!!)?.toInt()!!
        //
        val name = "${extractType(sessionMesg.sport!!)} - ${fitFile.name.replace(".FIT", "")}"
        //
        val resourceState = 2
        // The time at which the activity was started.
        val startDate: String = extractDate(sessionMesg.startTime?.timestamp!!)
        // The time at which the activity was started in the local timezone.
        val startDateLocal: String = extractDateLocal(sessionMesg.startTime?.timestamp!!)
        //
        val extractedStartLatLng = extractLatLng(sessionMesg.startPositionLat, sessionMesg.startPositionLong)
        val startLatlng: List<Double>? = extractedStartLatLng.ifEmpty {
            stream.latitudeLongitude?.data?.first()
        }
        //
        val timezone = ""
        //
        val deltas = stream.altitude?.data?.zipWithNext { a, b -> b - a }
        val sum = deltas?.filter { it > 0 }?.sumOf { it }
        val totalElevationGain: Double = sessionMesg.totalAscent?.toDouble() ?: sum!!
        //
        val type: String = extractType(sessionMesg.sport!!)
        //
        val utcOffset = 0.0
        //
        val workoutType = 0

        val activity = Activity(
            // The number of achievements gained during this activity
            achievementCount = 0,
            athlete = athlete,
            athleteCount = 1,
            averageSpeed = averageSpeed,
            averageCadence = averageCadence,
            averageHeartrate = averageHeartrate,
            maxHeartrate = maxHeartrate,
            averageWatts = averageWatts,
            commentCount = commentCount,
            commute = commute,
            deviceWatts = deviceWatts,
            displayHideHeartrateOption = displayHideHeartrateOption,
            distance = distance,
            elapsedTime = elapsedTime,
            elevHigh = elevHigh,
            elevLow = elevLow,
            endLatlng = endLatlng,
            externalId = externalId,
            flagged = false,
            fromAcceptedTag = false,
            gearId = null,
            hasHeartrate = false,
            hasKudoed = false,
            heartrateOptOut = false,
            id = id,
            kilojoules = kilojoules,
            kudosCount = 0,
            locationCity = locationCity,
            locationCountry = locationCountry,
            locationState = locationState,
            manual = manual,
            map = null,
            maxSpeed = maxSpeed,
            movingTime = movingTime,
            name = name,
            photoCount = 0,
            prCount = 0,
            private = true,
            resourceState = resourceState,
            startDate = startDate,
            startDateLocal = startDateLocal,
            startLatitude = 0.0,
            startLatlng = startLatlng,
            startLongitude = 0.0,
            timezone = timezone,
            totalElevationGain = totalElevationGain,
            totalPhotoCount = 0,
            trainer = false,
            type = type,
            uploadId = 0,
            uploadIdStr = null,
            utcOffset = utcOffset,
            visibility = "everyone",
            workoutType = workoutType,
        )

        activity.stream = stream

        return activity
    }

    private fun buildStream(recordMesgs: List<RecordMesg>): Stream {
        // distance
        val dataDistance = recordMesgs.map { recordMesg -> recordMesg.distance.toDouble() }.toMutableList()
        val streamDistance = Distance(
            data = dataDistance,
            originalSize = dataDistance.size,
            resolution = "high",
            seriesType = "distance"
        )

        //  time
        val startTime = recordMesgs.first().timestamp.timestamp
        val dataTime =
            recordMesgs.map { recordMesg -> (startTime - recordMesg.timestamp.timestamp).toInt() }.toMutableList()
        val streamTime = Time(
            data = dataTime,
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
        val dataAltitude: MutableList<Double> = if (recordMesgs.first().altitude != null) {
            recordMesgs.map { recordMesg ->
                recordMesg.altitude.toDouble()
            }.toMutableList()
        } else {
            generateDataAltitude(dataLatitudeLongitude)
        }
        val streamAltitude = Altitude(
            data = dataAltitude,
            originalSize = dataAltitude.size,
            resolution = "high",
            seriesType = "distance"
        )

        return Stream(streamDistance, streamTime, null, streamAltitude, streamLatitudeLongitude)
    }


    private fun generateDataAltitude(LatitudeLongitudeList: List<List<Double>>): MutableList<Double> {
        return srtmService.getElevation(LatitudeLongitudeList).toMutableList()
    }

    private fun extractLatLng(lat: Int?, lng: Int?): List<Double> {
        return if (lat != null && lng != null) {
            // 11930465 = (2^32 / 360)
            listOf(lat.toDouble() / 11930465, lng.toDouble() / 11930465)
        } else {
            emptyList()
        }
    }

    private fun extractType(sport: Sport): String {
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

    private fun extractElevLow(sessionMesg: SessionMesg): Double {
        return if (sessionMesg.minAltitude != null) {
            sessionMesg.minAltitude.toDouble()
        } else if (sessionMesg.enhancedMinAltitude != null) {
            sessionMesg.enhancedMinAltitude.toDouble()
        } else {
            0.0
        }
    }

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


