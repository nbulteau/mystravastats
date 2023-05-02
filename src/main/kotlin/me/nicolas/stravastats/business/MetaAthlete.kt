package me.nicolas.stravastats.business

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class MetaAthlete(
    val id: Long,
)