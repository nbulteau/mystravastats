package me.nicolas.stravastats.business.badges

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Stream
import java.io.File

fun loadColAgnelActivity(): Activity {
    val objectMapper = jacksonObjectMapper()
    var url = Thread.currentThread().contextClassLoader.getResource("colagnel-activity.json")
    var jsonFile = File(url!!.path)
    val activity = objectMapper.readValue(jsonFile, Activity::class.java)

    url = Thread.currentThread().contextClassLoader.getResource("colagnel-stream.json")
    jsonFile = File(url!!.path)
    activity.stream = objectMapper.readValue(jsonFile, Stream::class.java)

    return activity
}
