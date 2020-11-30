package me.nicolas.stravastats.infrastructure.dao


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Athlete(
    @JsonProperty("badge_type_id")
    val badgeTypeId: Int,
    @JsonProperty("city")
    val city: String?,
    @JsonProperty("country")
    val country: String?,
    @JsonProperty("created_at")
    val createdAt: String?,
    @JsonProperty("firstname")
    val firstname: String?,
    @JsonProperty("follower")
    val follower: Any?,
    @JsonProperty("friend")
    val friend: Any?,
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("lastname")
    val lastname: String?,
    @JsonProperty("premium")
    val premium: Boolean,
    @JsonProperty("profile")
    val profile: String?,
    @JsonProperty("profile_medium")
    val profileMedium: String?,
    @JsonProperty("resource_state")
    val resourceState: Int?,
    @JsonProperty("sex")
    val sex: String?,
    @JsonProperty("state")
    val state: String?,
    @JsonProperty("summit")
    val summit: Boolean,
    @JsonProperty("updated_at")
    val updatedAt: String?,
    @JsonProperty("username")
    val username: String?
)