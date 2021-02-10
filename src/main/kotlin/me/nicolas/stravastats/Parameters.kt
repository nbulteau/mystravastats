package me.nicolas.stravastats

import com.beust.jcommander.Parameter


class Parameters {

    @Parameter(names = ["-clientId"], required = true, description = "clientId")
    var clientId: String = "clientId"

    @Parameter(names = ["-clientSecret"], required = false, description = "clientSecret")
    var clientSecret: String? = null

    @Parameter(names = ["-code"], required = false, description = "authorization code")
    var code: String? = null

    @Parameter(names = ["-accessToken"], required = false, description = "Access token")
    var accessToken: String? = null

    @Parameter(names = ["-year"], required = false, description = "year")
    var year: Int? = null

    @Parameter(names = ["-csv"], required = false, description = "Export all activities in CSV files")
    var csv: Boolean = false

    @Parameter(names = ["-filter"], required = false, description = "Distance filter for CSV export")
    var filter: Double? = null
}