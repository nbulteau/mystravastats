package me.nicolas.stravastats.service.srtm

import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 *
 * The FileBasedSRTM object is the SRTM implementation of the Terrain interface
 *
 */
class SRTMFile(file: File) : AbstractFileBasedTerrain(file) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SRTMFile::class.java)

        private const val DATA_SIZE_BYTES = 2
    }

    var level: SRTMLevelEnum

    val tile: BoundingBox

    val southWestCorner: Point
        get() = Point(tile.south, tile.west)

    val northWestCorner: Point
        get() = Point(tile.north, tile.west)

    val northEastCorner: Point
        get() = Point(tile.north, tile.east)

    val southEastCorner: Point
        get() = Point(tile.south, tile.east)

    init {
        try {
            level = SRTMLevelEnum.fromFile(file)

            // init tile
            var name = file.name
            name = name.substringBefore(".")

            val southWest = Point()

            val northSouth = name.substring(1, 3).toInt()
            if (name.startsWith("N")) {
                southWest.latitude = northSouth.toDouble()
            } else {
                southWest.latitude = (-1 * northSouth).toDouble()
            }

            val westEast = name.substring(4, 7).toInt()
            if (name.contains("E")) {
                southWest.longitude = westEast.toDouble()
            } else {
                southWest.longitude = (-1 * westEast).toDouble()
            }
            val northEast = southWest.add(
                latitude = (level.rows - 1) * level.spacing, longitude = (level.columns - 1) * level.spacing
            )

            tile = BoundingBox(southWest, northEast)
        } catch (fileNotFoundException: FileNotFoundException) {
            if (LOGGER.isErrorEnabled) {
                LOGGER.error("could not find file $file", fileNotFoundException)
            }
            throw InstantiationException(fileNotFoundException.message)
        } catch (ioException: IOException) {
            if (LOGGER.isErrorEnabled) {
                LOGGER.error("could not read file $file", ioException)
            }
            throw InstantiationException(ioException.message)
        }
    }

    /**
     * Is the point contained within the data. This test does not check to see if the data contains the exact point,
     * but if the point is within the bounds of the data.
     *
     * @param point
     * @return
     */
    fun contains(point: Point): Boolean {
        return tile.contains(point)
    }

    fun getElevation(point: Point): Elevation {
        val resolution = level.spacing
        val rowAndColumn = rowAndColumn(point)
        val vertLocation = abs(rowAndColumn[0]).roundToInt()
        val hozLocation = abs(rowAndColumn[1]).roundToInt()
        val skipTo = (vertLocation * DATA_SIZE_BYTES * level.columns + hozLocation * DATA_SIZE_BYTES).toLong()

        var elevation: Double = try {
            if (skipTo >= file.length()) {
                throw CorruptTerrainException(
                    "ran past end of file requested move to was " + skipTo + " but file length is " + file.length()
                )
            }
            if (LOGGER.isTraceEnabled) {
                LOGGER.trace(
                    ("move to data element " + (skipTo / DATA_SIZE_BYTES + 1) + " out of ") + file.length() / DATA_SIZE_BYTES
                )
            }
            file.seek(skipTo)
            file.readShort().toDouble()
        } catch (ioException: IOException) {
            if (LOGGER.isErrorEnabled) {
                LOGGER.error("Error reading file", ioException)
            }
            throw CorruptTerrainException(ioException)
        }
        val actual = Point(
            latitude = southWestCorner.latitude + (level.columns - vertLocation - 1) * resolution,
            longitude = southWestCorner.longitude + hozLocation * resolution
        )

        if (elevation == -32768.0) {
            elevation = Double.NaN
        }

        return Elevation(elevation, actual)
    }

    /**
     * @param point
     * @return
     */
    private fun rowAndColumn(point: Point): DoubleArray {
        val resolution = level.spacing
        val row = level.rows - (point.latitude - southWestCorner.latitude) / resolution - 1
        val column = ((point.longitude - southWestCorner.longitude) / resolution)
        return doubleArrayOf(row, column)
    }
}