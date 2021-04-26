package me.nicolas.stravastats.core.csv

import me.nicolas.stravastats.business.Activity
import java.io.File
import java.io.FileWriter
import java.io.Writer

internal abstract class CSVExporter(activities: List<Activity>, val type: String) {

    val activities: List<Activity> = activities.filter { activity -> activity.type == type }

    fun export(clientId: String, year: Int) {
        // if no activities : nothing to do
        if (activities.isNotEmpty()) {
            val writer = FileWriter(File("$clientId-$type-$year.csv"))
            writer.use {
                generateHeader(writer)
                generateActivities(writer)
                generateFooter(writer)
            }
        }
    }

    protected abstract fun generateActivities(writer: FileWriter)
    protected abstract fun generateHeader(writer: FileWriter)
    protected abstract fun generateFooter(writer: FileWriter)
}


fun List<String>.writeCSVLine(writer: Writer) {
    writeLine(writer, this, ' ')
}

fun writeLine(writer: Writer, values: List<String>, customQuote: Char) {

    val separators = ';'
    var first = true

    val sb = StringBuilder()
    for (value in values) {
        if (!first) {
            sb.append(separators)
        }
        if (customQuote == ' ') {
            sb.append(followCVSFormat(value))
        } else {
            sb.append(customQuote).append(followCVSFormat(value)).append(customQuote)
        }
        first = false
    }
    sb.append("\n")
    writer.append(sb.toString())
}

//https://tools.ietf.org/html/rfc4180
private fun followCVSFormat(value: String): String {

    var result = value
    if (result.contains("\"")) {
        result = result.replace("\"", "\"\"")
    }
    return result
}