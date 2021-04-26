package me.nicolas.stravastats.core.charts

import me.nicolas.stravastats.business.Activity
import java.time.LocalDate

class ChartHelper {

    companion object {
        fun getActivitiesByMonth(activities: List<Activity>): Map<String, List<Activity>> {
            val activitiesByMonth = activities
                .groupBy { activity -> activity.startDateLocal.subSequence(5, 7).toString() }
                .toMutableMap()

            // Add months without activities
            (1..12).forEach {
                if (!activitiesByMonth.contains("$it".padStart(2, '0'))) {
                    activitiesByMonth["$it".padStart(2, '0')] = emptyList()
                }
            }

            return activitiesByMonth.toSortedMap()
        }

        fun getActivitiesByDay(activities: List<Activity>, year: Int): Map<String, List<Activity>> {
            val activitiesGroupedByDay = activities
                .groupBy { activity -> activity.startDateLocal.subSequence(5, 10).toString() }

            val activitiesByDay = activitiesGroupedByDay.toMutableMap()
            // init current date to first of the year
            var currentDate = LocalDate.ofYearDay(year, 1)
            for (i in (0..365 + if (currentDate.isLeapYear) 1 else 0)) {
                currentDate = currentDate.plusDays(1L)
                val dayString =
                    "${currentDate.monthValue}".padStart(2, '0') + "-" + "${currentDate.dayOfMonth}".padStart(2, '0')
                if (!activitiesByDay.containsKey(dayString)) {
                    activitiesByDay[dayString] = emptyList()
                }
            }

            return activitiesByDay.toSortedMap()
        }
    }
}