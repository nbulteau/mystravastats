package me.nicolas.stravastats.core.business

fun Int.formatSeconds() = String.format("%02d:%02d", (this % 3600) / 60, this % 60)

fun Double.formatHundredths(): String? {
    var min = ((this % 3600) / 60).toInt()
    var sec = (this % 60).toInt()
    var hnd = ((this - (min * 60) - sec) * 100 + 0.5).toInt()
    if (hnd == 100) {
        hnd = 0
        if (++sec == 60) {
            sec = 0
            ++min
        }
    }
    return String.format("%d:%02d.%02d", min, sec, hnd)
}