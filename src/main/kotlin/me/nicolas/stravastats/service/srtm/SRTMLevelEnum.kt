package me.nicolas.stravastats.service.srtm

import java.io.File

/**
 *
 * Description: The SRTMLevelEnum is an enumerator that defines what SRTM resolutions are supported
 */
enum class SRTMLevelEnum(spacing: Double) {

    SRTM3(3.0), SRTM1(1.0);

    companion object {
        fun fromFile(file: File): SRTMLevelEnum {
            return if (file.length() > 25000000) {
                SRTM1
            } else {
                SRTM3
            }
        }
    }

    val rows: Int

    val columns: Int

    //in degrees
    val spacing: Double = spacing / 3600.0

    init {
        rows = (1.0 / this.spacing + this.spacing * 2).toInt() + 1
        columns = (1.0 / this.spacing + this.spacing * 2).toInt() + 1
    }
}