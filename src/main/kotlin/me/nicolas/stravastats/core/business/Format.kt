package me.nicolas.stravastats.core.business

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

var inFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
var outFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE dd MMMM yyyy - HH:mm:ss")

fun Int.formatSeconds(): String {
    val hours = (this - (this % 3600)) / 3600
    if (hours != 0) {
        return String.format("%02dh %02dm %02ds", hours, (this % 3600) / 60, this % 60)
    }
    return String.format("%02dm %02ds", (this % 3600) / 60, this % 60)
}

fun Double.formatSeconds() = String.format("%d:%02d", ((this % 3600) / 60).toInt(), (this % 60).toInt())

fun String.formatDate(): String = LocalDateTime.parse(this, inFormatter).format(outFormatter)