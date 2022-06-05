package me.nicolas.stravastats.service.srtm

import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile

/**
 *
 * The AbstractFileBasedTerrain object is the abstract implementation of the Terrain interface
 */
abstract class AbstractFileBasedTerrain(file: File) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AbstractFileBasedTerrain::class.java)
    }

    protected val file: RandomAccessFile

    private var closed = false

    init {
        try {
            this.file = RandomAccessFile(file, "r")
        } catch (fileNotFoundException: FileNotFoundException) {
            if (LOGGER.isErrorEnabled) {
                LOGGER.error("could not find file $file", fileNotFoundException)
            }
            throw InstantiationException(fileNotFoundException.message)
        }
    }

    /**
     * free up any resources
     */
    fun destroy() {
        if (!closed) {
            closed = true
            try {
                file.close()
            } catch (e: IOException) {
                if (LOGGER.isWarnEnabled) {
                    LOGGER.warn("failed to close random access file", e)
                }
            }
        }
    }
}