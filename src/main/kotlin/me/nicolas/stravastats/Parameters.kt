package me.nicolas.stravastats

import com.beust.jcommander.Parameter


class Parameters {

    @Parameter(names = ["-file"], required = false, description = "file")
    var file: String? = null

    @Parameter(names = ["-code"], required = false, description = "authorization code")
    var code: String? = null

    @Parameter(names = ["-accessToken"], required = false, description = "Access token")
    var accessToken: String? = null

    @Parameter(names = ["-clientId"], required = false, description = "clientId")
    var clientId: String = "clientId"

    @Parameter(names = ["-clientSecret"], required = false, description = "clientSecret")
    var clientSecret: String = "clientSecret"

    @Parameter(names = ["-year"], required = false, description = "year")
    var year: Int = 2020

    @Parameter(names = ["-filter"], required = false, description = "Distance filter")
    var filter: Double? = null
}