package me.nicolas.stravastats.core

import java.io.Writer

private const val DEFAULT_SEPARATOR = ';'


fun List<String>.writeCSVLine(writer: Writer) {
    writeLine(writer, this, DEFAULT_SEPARATOR, ' ')
}

//https://tools.ietf.org/html/rfc4180
private fun followCVSformat(value: String): String {
    var result = value
    if (result.contains("\"")) {
        result = result.replace("\"", "\"\"")
    }
    return result
}

fun writeLine(writer: Writer, values: List<String>, separators: Char, customQuote: Char) {
    var separators = separators
    var first = true

    //default customQuote is empty
    if (separators == ' ') {
        separators = DEFAULT_SEPARATOR
    }
    val sb = StringBuilder()
    for (value in values) {
        if (!first) {
            sb.append(separators)
        }
        if (customQuote == ' ') {
            sb.append(followCVSformat(value))
        } else {
            sb.append(customQuote).append(followCVSformat(value)).append(customQuote)
        }
        first = false
    }
    sb.append("\n")
    writer.append(sb.toString())
}
