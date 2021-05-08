package me.nicolas.stravastats.business

data class Bike(
    val distance: Int,
    val id: String,
    val name: String,
    val primary: Boolean,
    val resource_state: Int
)