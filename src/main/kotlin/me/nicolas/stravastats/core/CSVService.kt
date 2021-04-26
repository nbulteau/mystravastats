package me.nicolas.stravastats.core

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.core.csv.RideCSVExporter
import me.nicolas.stravastats.core.csv.HikeCSVExporter
import me.nicolas.stravastats.core.csv.InlineSkateCSVExporter
import me.nicolas.stravastats.core.csv.RunCSVExporter

internal class CSVService {

    fun exportCSV(clientId: String, activities: List<Activity>, filter: Double?) {
        activities
            .groupBy { activity ->
                activity.startDateLocal.subSequence(0, 4).toString()
            } // year by year
            .forEach { map: Map.Entry<String, List<Activity>> ->
                if (filter != null) {
                    exportCSV(clientId, filterActivities(map.value, filter), map.key.toInt())
                } else {
                    exportCSV(clientId, map.value, map.key.toInt())
                }
            }
    }

    private fun exportCSV(clientId: String, activities: List<Activity>, year: Int) {
        print("* Export activities for $year [")

        print("Ride")
        val rideCSVExporter = RideCSVExporter(activities = activities)
        rideCSVExporter.export(clientId = clientId, year = year)

        print(", Run")
        val runCSVExporter = RunCSVExporter(activities = activities)
        runCSVExporter.export(clientId = clientId, year = year)

        print(", InlineSkate")
        val inlineSkateCSVExporter = InlineSkateCSVExporter(activities = activities)
        inlineSkateCSVExporter.export(clientId = clientId, year = year)

        print(", Hike")
        val hikeCSVExporter = HikeCSVExporter(activities = activities)
        hikeCSVExporter.export(clientId = clientId, year = year)

        println("]")
    }

    private fun filterActivities(activities: List<Activity>, filter: Double): List<Activity> {
        val lowBoundary = filter - (5 * filter / 100)
        val highBoundary = filter + (5 * filter / 100)
        return activities.filter { activity -> activity.distance > lowBoundary && activity.distance < highBoundary }
    }
}