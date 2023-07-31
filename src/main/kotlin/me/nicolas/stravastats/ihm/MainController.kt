package me.nicolas.stravastats.ihm

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.chart.XYChart
import javafx.scene.control.Hyperlink
import javafx.scene.image.ImageView
import me.nicolas.stravastats.business.*
import me.nicolas.stravastats.business.badges.Badge
import me.nicolas.stravastats.business.badges.DistanceBadge
import me.nicolas.stravastats.business.badges.ElevationBadge
import me.nicolas.stravastats.business.badges.MovingTimeBadge
import me.nicolas.stravastats.ihm.detailview.ActivityDetailView
import me.nicolas.stravastats.ihm.detailview.ActivityWithGradientDetailView
import me.nicolas.stravastats.service.*
import me.nicolas.stravastats.service.statistics.ActivityStatistic
import me.nicolas.stravastats.service.statistics.Statistic
import me.nicolas.stravastats.service.statistics.calculateBestElevationForDistance
import me.nicolas.stravastats.service.statistics.calculateBestTimeForDistance
import me.nicolas.stravastats.utils.GenericCache
import me.nicolas.stravastats.utils.SoftCache
import me.nicolas.stravastats.utils.formatDate
import tornadofx.Controller
import kotlin.collections.component1
import kotlin.collections.component2


class MainController(private val clientId: String, private val activities: ObservableList<Activity>) : Controller() {

    private val stravaService = StravaService.getInstance()

    private val statsService = StatisticsService()

    private val chartsService = ChartsService()

    private val csvService = CSVService()

    private val badgesService = BadgesService()

    private val filteredActivitiesCache: GenericCache<String, List<Activity>> = SoftCache()

    private val famousClimbClimbBadgesCache: GenericCache<String, List<List<BadgeDisplay>>> = SoftCache()


    fun generateCSV(year: Int?) {

        val activitiesForYear: List<Activity> = if (year != null) {
            activities.groupBy { activity ->
                activity.startDateLocal.subSequence(0, 4).toString()
            }["$year"] ?: emptyList()
        } else {
            activities
        }

        csvService.exportCSV(clientId, activitiesForYear, (year?.toString() ?: ""))
    }

    fun generateCharts(year: Int?) {
        chartsService.buildCharts(activities, year)
    }

    fun getFilteredActivities(activityType: String, year: Int?): List<Activity> {
        val filteredActivities = filteredActivitiesCache["$activityType-$year"] ?: this.activities
            .filterActivitiesByType(activityType)
            .filterActivitiesByYear(year)
        filteredActivitiesCache["$activityType-$year"] = filteredActivities

        return filteredActivities
    }

    fun getActiveDaysByActivityTypeByYear(activityType: String, year: Int?): Map<String, Int> {

        val filteredActivities = getFilteredActivities(activityType, year)

        return filteredActivities
            .groupBy { activity -> activity.startDateLocal.substringBefore('T') }
            .mapValues { (_, activities) -> activities.sumOf { activity -> activity.distance / 1000 } }
            .mapValues { entry -> entry.value.toInt() }
            .toMap()
    }

    fun getActiveDaysByActivityType(activityType: String): Map<String, Int> {

        val filteredActivities = this.activities
            .filterActivitiesByType(activityType)

        return filteredActivities
            .groupBy { activity -> activity.startDateLocal.substringBefore('T') }
            .mapValues { (_, activities) -> activities.sumOf { activity -> activity.distance / 1000 } }
            .mapValues { entry -> entry.value.toInt() }
            .toMap()
    }

    fun getActivitiesByYear(activityType: String): Map<String, List<Activity>> {

        val filteredActivities = this.activities.filterActivitiesByType(activityType)

        return ActivityHelper.groupActivitiesByYear(filteredActivities)
    }

    fun getActivitiesToDisplay(activityType: String, year: Int?): ObservableList<ActivityDisplay> {

        val filteredActivities = getFilteredActivities(activityType, year)

        return buildActivitiesToDisplay(filteredActivities)
    }

    fun getStatisticsToDisplay(activityType: String, year: Int?): ObservableList<StatisticDisplay> {

        val filteredActivities = getFilteredActivities(activityType, year)

        val statistics = when (activityType) {
            Ride -> statsService.computeRideStatistics(filteredActivities)
            Commute -> statsService.computeCommuteStatistics(filteredActivities)
            Run -> statsService.computeRunStatistics(filteredActivities)
            InlineSkate -> statsService.computeInlineSkateStatistics(filteredActivities)
            Hike -> statsService.computeHikeStatistics(filteredActivities)
            AlpineSki -> statsService.computeAlpineSkiStatistics(filteredActivities)

            else -> emptyList()
        }

        return buildStatisticsToDisplay(statistics)
    }


    fun getGeneralBadgesSetToDisplay(activityType: String, year: Int?): List<List<BadgeDisplay>> {
        val filteredActivities = getFilteredActivities(activityType, year)

        val badgesSets = mutableListOf<List<BadgeDisplay>>()
        when (activityType) {
            Ride -> {
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = DistanceBadge.rideBadgeSet.check(filteredActivities),
                        imageViewUrl = "images/racing.png"
                    )
                )
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = ElevationBadge.rideBadgeSet.check(filteredActivities),
                        imageViewUrl = "images/cycling.png"
                    )
                )
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = MovingTimeBadge.movingTimeBadgesSet.check(filteredActivities),
                        imageViewUrl = "images/stopwatch.png"
                    )
                )
            }

            Run -> {
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = DistanceBadge.runBadgeSet.check(filteredActivities),
                        imageViewUrl = "images/run.png"
                    )
                )
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = ElevationBadge.runBadgeSet.check(filteredActivities),
                        imageViewUrl = "images/run.png"
                    )
                )
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = MovingTimeBadge.movingTimeBadgesSet.check(filteredActivities),
                        imageViewUrl = "images/stopwatch.png"
                    )
                )
            }
        }
        return badgesSets
    }

    fun getFamousClimbBadgesSetToDisplay(activityType: String, year: Int?): List<List<BadgeDisplay>> {
        val famousClimbClimbBadges =
            famousClimbClimbBadgesCache["$activityType-$year"] ?: buildFamousClimbBadgesSet(activityType, year)
        famousClimbClimbBadgesCache["$activityType-$year"] = famousClimbClimbBadges

        return famousClimbClimbBadges
    }

    private fun buildFamousClimbBadgesSet(activityType: String, year: Int?): List<List<BadgeDisplay>> {
        val filteredActivities = getFilteredActivities(activityType, year)

        val badgesSets = mutableListOf<List<BadgeDisplay>>()
        when (activityType) {
            Ride -> {
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = badgesService.getAlpesFamousBadges(filteredActivities),
                        imageViewUrl = "images/cycling.png"
                    )
                )
                badgesSets.add(
                    buildBadgesToDisplay(
                        badgesList = badgesService.getPyreneesFamousBadges(filteredActivities),
                        imageViewUrl = "images/cycling.png"
                    )
                )
            }
        }

        return badgesSets
    }

    fun buildDistanceByMonthsSeries(activityType: String, year: Int): ObservableList<XYChart.Data<String, Number>> {

        val filteredActivities = getFilteredActivities(activityType, year)

        return buildDistanceSeries(ActivityHelper.groupActivitiesByMonth(filteredActivities))
    }

    fun buildElevationGainByMonthsSeries(activityType: String, year: Int): ObservableList<XYChart.Data<String, Number>> {

        val filteredActivities = getFilteredActivities(activityType, year)

        return buildElevationGainSeries(ActivityHelper.groupActivitiesByMonth(filteredActivities))
    }

    fun buildDistanceByWeeksSeries(activityType: String, year: Int): ObservableList<XYChart.Data<String, Number>> {

        val filteredActivities = getFilteredActivities(activityType, year)

        return buildDistanceSeries(ActivityHelper.groupActivitiesByWeek(filteredActivities))
    }

    fun buildElevationGainByWeeksSeries(activityType: String, year: Int): ObservableList<XYChart.Data<String, Number>> {

        val filteredActivities = getFilteredActivities(activityType, year)

        return buildElevationGainSeries(ActivityHelper.groupActivitiesByWeek(filteredActivities))
    }

    fun buildDistanceByDaysSeries(activityType: String, year: Int): ObservableList<XYChart.Data<String, Number>> {

        val filteredActivities = getFilteredActivities(activityType, year)

        return buildDistanceSeries(ActivityHelper.groupActivitiesByDay(filteredActivities, year))
    }

    private fun buildDistanceSeries(activities: Map<String, List<Activity>>): ObservableList<XYChart.Data<String, Number>> {
        val distance: Map<String, Double> = activities.mapValues { (_, activities) ->
            activities.sumOf { activity ->
                activity.distance / 1000
            }
        }

        return FXCollections.observableList(distance.map { entry ->
            XYChart.Data(entry.key, entry.value)
        })
    }

    private fun buildElevationGainSeries(activities: Map<String, List<Activity>>): ObservableList<XYChart.Data<String, Number>> {
        val totalElevationGain: Map<String, Double> = activities.mapValues { (_, activities) ->
            activities.sumOf { activity ->
                activity.totalElevationGain
            }
        }

        return FXCollections.observableList(totalElevationGain.map { entry ->
            XYChart.Data(entry.key, entry.value)
        })
    }

    private fun List<Activity>.filterActivitiesByType(activityType: String): List<Activity> {
        return if (activityType == Commute) {
            this.filter { activity -> activity.type == Ride && activity.commute }
        } else {
            this.filter { activity -> activity.type == activityType && !activity.commute }
        }
    }

    private fun List<Activity>.filterActivitiesByYear(year: Int?): List<Activity> {
        return if (year == null) {
            this
        } else {
            this.filter { activity -> activity.startDateLocal.subSequence(0, 4).toString().toInt() == year }
        }
    }

    private fun buildBadgesToDisplay(
        badgesList: List<Triple<Badge, Activity?, Boolean>>,
        imageViewUrl: String
    ): List<BadgeDisplay> {

        val badges = badgesList.map { triple ->
            val isCompleted = triple.third
            val activity = triple.second
            val badge = triple.first

            val imageView = ImageView(imageViewUrl)
                .apply {
                    if (!isCompleted) {
                        opacity = 0.15
                    }
                    fitWidth = 110.0
                    isPreserveRatio = true
                    isSmooth = true
                    isCache = true
                }

            val hyperlink = Hyperlink("", imageView)
                .apply {
                    onAction = if (triple.second != null) {
                        buildDisplayActivityDetailEventHandler(activity)
                    } else {
                        EventHandler {
                        }
                    }
                }

            BadgeDisplay(badge.toString(), hyperlink, badge)
        }

        return badges
    }

    private fun buildStatisticsToDisplay(statistics: List<Statistic>): ObservableList<StatisticDisplay> {

        val activityStatistics = statistics.map { statistic ->
            when (statistic) {
                is ActivityStatistic -> {
                    val hyperlink = if (statistic.activity != null) {
                        Hyperlink(statistic.activity.toString()).apply {
                            onAction = buildDisplayActivityDetailEventHandler(statistic.activity)
                        }
                    } else {
                        null
                    }

                    StatisticDisplay(statistic.name, statistic.value, hyperlink)
                }

                else -> StatisticDisplay(statistic.name, statistic.toString(), null)
            }
        }
        return FXCollections.observableArrayList(activityStatistics)
    }

    private fun buildActivitiesToDisplay(activities: List<Activity>): ObservableList<ActivityDisplay> {

        val activitiesToDisplay = activities.map { activity ->
            val hyperlink = if (activity.stream?.latitudeLongitude != null) {
                Hyperlink(activity.name).apply {
                    onAction = buildDisplayActivityDetailEventHandler(activity)
                }
            } else {
                // No maps to display
                Hyperlink(activity.name)
            }

            ActivityDisplay(
                hyperlink,
                activity.distance,
                activity.elapsedTime,
                activity.totalElevationGain,
                activity.calculateTotalDescentGain(),
                activity.averageSpeed,
                activity.calculateBestTimeForDistance(1000.0)?.getFormattedSpeed() ?: "",
                activity.calculateBestElevationForDistance(250.0)?.getFormattedGradient() ?: "",
                activity.calculateBestElevationForDistance(500.0)?.getFormattedGradient() ?: "",
                activity.startDateLocal.formatDate()
            )
        }
        return FXCollections.observableArrayList(activitiesToDisplay)
    }

    private fun buildDisplayActivityDetailEventHandler(activity: Activity?): EventHandler<ActionEvent> =
        EventHandler {
            if ((activity != null)
                && (activity.stream?.latitudeLongitude?.data != null)
                && (activity.stream?.distance?.data != null)
                && (activity.stream?.altitude?.data != null)
            ) {
                val segmentEfforts = getSegmentEfforts(activity)

                if (activity.type == Ride || activity.type == Hike) {
                    ActivityWithGradientDetailView(
                        activity,
                        activity.stream?.latitudeLongitude?.data!!,
                        activity.stream?.distance?.data!!,
                        activity.stream?.altitude?.data!!,
                        segmentEfforts
                    ).openModal()
                } else {
                    ActivityDetailView(
                        activity,
                        activity.stream?.latitudeLongitude?.data!!,
                        activity.stream?.distance?.data!!,
                        activity.stream?.altitude?.data!!,
                        segmentEfforts
                    ).openModal()
                }
            }
        }

    private fun getSegmentEfforts(activity: Activity): List<SegmentEffort> {
        if (stravaService != null) {
            val year = activity.startDate.substring(0..3).toInt()
            val optionalDetailedActivity = stravaService.getActivity(year, activity.id)
            if (optionalDetailedActivity.isPresent) {
                return optionalDetailedActivity.get().segmentEfforts.distinct()
            }
        }

        return emptyList()
    }
}
