package me.nicolas.stravastats.service.csv

import me.nicolas.stravastats.business.Activity
import java.io.File
import java.io.FileWriter

internal abstract class CSVExporter(val clientId: String, activities: List<Activity>, val year: Int, val type: String) {

    protected val activities: List<Activity> = activities.filter { activity -> activity.type == type }

    private val writer = FileWriter(File("$clientId-$type-$year.csv"))

    fun export() {
        // if no activities : nothing to do
        if (activities.isNotEmpty()) {
            writer.use {
                generateHeader()
                generateActivities()
                generateFooter()
            }
        }
    }

    protected abstract fun generateActivities()
    protected abstract fun generateHeader()
    protected abstract fun generateFooter()

    protected fun writeCSVLine(values: List<String>, customQuote: Char = ' ') {

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

}


