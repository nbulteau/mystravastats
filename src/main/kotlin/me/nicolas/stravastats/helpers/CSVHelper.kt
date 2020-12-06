package me.nicolas.stravastats.helpers

import java.io.Writer


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