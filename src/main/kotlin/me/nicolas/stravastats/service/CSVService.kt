package me.nicolas.stravastats.service

import me.nicolas.stravastats.business.*
import me.nicolas.stravastats.service.csv.HikeCSVExporter
import me.nicolas.stravastats.service.csv.InlineSkateCSVExporter
import me.nicolas.stravastats.service.csv.RideCSVExporter
import me.nicolas.stravastats.service.csv.RunCSVExporter

internal class CSVService {

    fun exportCSV(clientId: String, activities: List<Activity>) {
        activities
            .groupBy { activity ->
                activity.startDateLocal.subSequence(0, 4).toString()
            } // year by year
            .forEach { map: Map.Entry<String, List<Activity>> ->
                exportCSV(clientId, map.value, map.key.toInt())
            }
    }

    private fun exportCSV(clientId: String, activities: List<Activity>, year: Int) {
        print("* Export activities for $year [")

        print(Ride)
        val rideCSVExporter = RideCSVExporter(clientId = clientId, activities = activities, year = year)
        rideCSVExporter.export()

        print(", $Run")
        val runCSVExporter = RunCSVExporter(clientId = clientId, activities = activities, year = year)
        runCSVExporter.export()

        print(", $InlineSkate")
        val inlineSkateCSVExporter = InlineSkateCSVExporter(clientId = clientId, activities = activities, year = year)
        inlineSkateCSVExporter.export()

        print(", $Hike")
        val hikeCSVExporter = HikeCSVExporter(clientId = clientId, activities = activities, year = year)
        hikeCSVExporter.export()

        println("]")
    }

}