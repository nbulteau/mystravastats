package me.nicolas.stravastats.business

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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

fun loadZwiftActivity(): Activity {
    val objectMapper = jacksonObjectMapper()
    var url = Thread.currentThread().contextClassLoader.getResource("zwift-activity.json")
    var jsonFile = File(url!!.path)
    val activity = objectMapper.readValue(jsonFile, Activity::class.java)

    url = Thread.currentThread().contextClassLoader.getResource("zwift-stream.json")
    jsonFile = File(url!!.path)
    activity.stream = objectMapper.readValue(jsonFile, Stream::class.java)

    return activity
}







