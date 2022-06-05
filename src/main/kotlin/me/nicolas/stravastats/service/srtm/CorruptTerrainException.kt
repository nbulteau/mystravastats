package me.nicolas.stravastats.service.srtm

/**
 *
 * Description: The CorruptDTEDFileException object is throw when a DTED file seems to be corrupted
 *
 */
class CorruptTerrainException : Exception {

    companion object {
        private const val MESSAGE = "currupt file"
    }

    /**
     * @param message
     */
    @JvmOverloads
    constructor(message: String = MESSAGE) : super(message)

    /**
     * @param cause
     */
    constructor(cause: Throwable) : super(MESSAGE, cause)

    /**
     * @param message
     * @param cause
     */
    constructor(message: String, cause: Throwable?) : super(message, cause)
}