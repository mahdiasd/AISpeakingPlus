package ir.aispeaking.data.mapper.scenario

import ir.aispeaking.data.mapper.user.toGender
import ir.aispeaking.domain.model.scenario.Role
import ir.aispeaking.domain.model.scenario.Scenario
import ir.aispeaking.domain.model.scenario.ScenarioSummary
import ir.aispeaking.domain.model.user.Gender
import ir.aispeaking.network.dto.scenario.ScenarioResponse
import ir.aispeaking.network.dto.scenario.ScenarioSummaryResponse
import ir.aispeaking.utils.time.toInstant
import kotlinx.collections.immutable.toImmutableList

fun ScenarioResponse.toDomain(): Scenario {
    return Scenario(
        id = id,
        categoryId = categoryId,
        title = title,
        description = description,
        imageUrl = imageUrl,
        aiName = aiName,
        aiAvatar = aiAvatar,
        score = points,
        aiGender = gender.toGender() ?: Gender.Man,
        tasks = tasks.map { it.toDomain() }.toImmutableList(),
        createdAt = createdAt.toInstant(),
        persianDescription = persianDescription,
        starter = starter.toRole(),
        persianTitle = persianTitle
    )
}

fun String.toRole(): Role {
    return Role.entries.find { it.key.equals(this, true) } ?: throw IllegalArgumentException("Unknown role: $this")
}

fun ScenarioSummaryResponse.toDomain(): ScenarioSummary {
    return ScenarioSummary(
        id = id,
        title = title,
        imageUrl = imageUrl,
        aiAvatar = aiAvatar,
        score = score ?: 0
    )
}