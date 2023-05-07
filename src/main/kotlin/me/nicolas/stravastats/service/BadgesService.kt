package me.nicolas.stravastats.service

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.badges.Badge
import me.nicolas.stravastats.business.badges.BadgeSet
import me.nicolas.stravastats.business.badges.FamousClimb
import me.nicolas.stravastats.business.badges.FamousClimbBadge
import java.nio.file.Path

class BadgesService {

        private val objectMapper = jacksonObjectMapper()

        private val alpes: BadgeSet = loadBadgeSet("alpes", Path.of("famous_climb/alpes.json"))
        private val pyrenees: BadgeSet = loadBadgeSet("pyrenees",  Path.of("famous_climb/pyrenees.json"))

        private fun loadBadgeSet(name: String, climbsJsonFilePath: Path): BadgeSet {
            val badgeSet: BadgeSet

            try {
                val url = climbsJsonFilePath.toUri().toURL()
                val famousClimbs = objectMapper.readValue(url, Array<FamousClimb>::class.java).toList()
                badgeSet = BadgeSet(name, famousClimbs.flatMap { famousClimb ->
                    famousClimb.alternatives.map { alternative ->
                        FamousClimbBadge(
                            name = famousClimb.name,
                            label = "${famousClimb.name} from ${alternative.name}",
                            topOfTheAscent = famousClimb.topOfTheAscent,
                            start = famousClimb.geoCoordinate,
                            end = alternative.geoCoordinate,
                            difficulty = alternative.difficulty,
                            length = alternative.length,
                            totalAscent = alternative.totalAscent,
                            averageGradient = alternative.averageGradient
                        )
                    }
                }.toList())
            } catch (jsonMappingException: JsonMappingException) {
                throw RuntimeException("Something was wrong while reading BadgeSet", jsonMappingException)
            }

            return badgeSet
        }

    fun getAlpesFamousBadges(activities: List<Activity>): List<Triple<Badge, Activity?, Boolean>> =
        alpes.check(activities)

    fun getPyreneesFamousBadges(activities: List<Activity>): List<Triple<Badge, Activity?, Boolean>> =
        pyrenees.check(activities)
}
