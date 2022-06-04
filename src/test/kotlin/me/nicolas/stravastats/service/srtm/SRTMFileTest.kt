package me.nicolas.stravastats.service.srtm

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class SRTMFileTest {

    private val file = File("E:\\workspace\\dem4j\\target\\srtm3/N48W002.hgt")

    private lateinit var terrainFile: SRTMFile

    @BeforeEach
    fun setUp() {
        try {
            terrainFile = SRTMFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            fail()
        }
    }

    /**
     *
     */
    @AfterEach
    fun tearDown() {
        try {
            terrainFile.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    @Throws(Exception::class)
    fun myTest() {
        val pointNW: Point = terrainFile.northWestCorner
        println("NW : $pointNW")
        val pointNE: Point = terrainFile.northEastCorner
        println("NE : $pointNE")
        val pointSW: Point = terrainFile.southWestCorner
        println("SW : $pointSW")
        val pointSE: Point = terrainFile.southEastCorner
        println("SE : $pointSE")

        val point = Point(48.1563535, -1.5806757)
        if (terrainFile.contains(point)) {
            println(terrainFile.getElevation(point))
        }
    }
}