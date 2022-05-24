package me.nicolas.stravastats.service

import me.nicolas.stravastats.business.*
import me.nicolas.stravastats.service.csv.*

internal class CSVService {

    fun exportCSV(clientId: String, activities: List<Activity>, year: String) {

        print("* Export activities for $year [")

        print(Ride)
        val rideCSVExporter = RideCSVExporter(clientId = clientId, activities = activities, year = year)
        rideCSVExporter.export()

        print(", $Run")
        val runCSVExporter = RunCSVExporter(clientId = clientId, activities = activities, year = year)
        runCSVExporter.export()

        print(", $InlineSkate")
        val inlineSkateCSVExporter =
            InlineSkateCSVExporter(clientId = clientId, activities = activities, year = year)
        inlineSkateCSVExporter.export()

        print(", $Hike")
        val hikeCSVExporter = HikeCSVExporter(clientId = clientId, activities = activities, year = year)
        hikeCSVExporter.export()

        print(", $AlpineSki")
        val alpineSkiCSVExporter =
            AlpineSkiCSVExporter(clientId = clientId, activities = activities, year = year)
        alpineSkiCSVExporter.export()

        println("]")
    }

}