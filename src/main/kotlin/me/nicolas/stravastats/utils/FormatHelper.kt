package me.nicolas.stravastats.utils

import me.nicolas.stravastats.business.Run
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

var timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

var inDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

var outDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE dd MMMM yyyy - HH:mm")

var dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE dd MMMM yyyy")

fun String.formatDate(): String = LocalDateTime.parse(this, inDateTimeFormatter).format(outDateTimeFormatter)

fun Int.formatSeconds(): String {
    val hours = (this - (this % 3600)) / 3600
    val min = ((this % 3600) / 60)

    if (hours == 0) {
        if (min == 0) {
            return String.format("%02ds", this % 60)
        }
        return String.format("%02dm %02ds", min, this % 60)
    }
    return String.format("%02dh %02dm %02ds", hours, min, this % 60)
}

fun Double.formatSeconds(): String {
    var min = ((this % 3600) / 60).toInt()
    var sec = (this % 60).toInt()
    val hnd = ((this - min * 60 - sec) * 100 + 0.5).toInt()
    if (hnd == 100) {
        sec++
        if (sec == 60) {
            sec = 0
            ++min
        }
    }
    return String.format("%d'%02d", min, sec)
}

/**
 * Format average speed (m/s)
 */
fun Double.formatSpeed(activityType: String): String {
    return if (activityType == Run) {
        "${(1000 / this).formatSeconds()}/km"
    } else {
        "%.02f km/h".format(this * 3.6)
    }
}




