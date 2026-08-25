package ir.aispeaking.data.mapper.scenario

import ir.aispeaking.data.mapper.user.toGender
import ir.aispeaking.domain.model.scenario.Scenario
import ir.aispeaking.domain.model.scenario.ScenarioTask
import ir.aispeaking.domain.model.user.Gender
import ir.aispeaking.storage.model.scenario.SharedScenario
import ir.aispeaking.storage.model.scenario.SharedScenarioTask
import ir.aispeaking.utils.time.toInstant
import ir.aispeaking.utils.time.toInstantOrNull
import kotlinx.collections.immutable.toImmutableList

fun SharedScenarioTask.toDomain(): ScenarioTask {
    return ScenarioTask(
        id = this.id,
        scenarioId = this.scenarioId,
        persianDescription = this.persianDescription,
        description = this.description,
        createdAt = this.createdAt.toInstantOrNull()
    )
}

fun ScenarioTask.toShared(): SharedScenarioTask {
    return SharedScenarioTask(
        id = this.id,
        scenarioId = this.scenarioId,
        description = this.description,
        persianDescription = this.persianDescription,
        createdAt = this.createdAt?.toString()
    )
}

fun SharedScenario.toDomain(): Scenario {
    return Scenario(
        id = this.id,
        categoryId = this.categoryId,
        title = this.title,
        description = this.description,
        imageUrl = this.imageUrl,
        aiName = this.aiName,
        aiAvatar = this.aiAvatar,
        score = this.points,
        aiGender = gender.toGender() ?: Gender.Woman,
        tasks = this.tasks.map { it.toDomain() }.toImmutableList(),
        createdAt = this.createdAt.toInstant(),
        starter = this.starter.toRole(),
        isChallenge = isChallenge
    )
}

fun Scenario.toShared(): SharedScenario {
    return SharedScenario(
        id = this.id,
        categoryId = this.categoryId,
        title = this.title,
        description = this.description,
        imageUrl = this.imageUrl,
        aiName = this.aiName,
        aiAvatar = this.aiAvatar,
        points = this.score,
        tasks = this.tasks.map { it.toShared() }.toList(), // Convert ScenarioTask to SharedScenarioTask
        createdAt = this.createdAt.toString(),
        gender = aiGender.name,
        starter = starter.name,
        isChallenge = isChallenge
    )
}