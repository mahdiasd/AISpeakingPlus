package ir.speaking.feature.challenge.challenge.mapper

import ir.speaking.feature.challenge.challenge.model.Challenge
import ir.speaking.feature.challenge.progress.model.ChallengeProgress
import ir.speaking.feature.challenge.task.model.ChallengeTask
import ir.speaking.feature.scenario.progress.dto.response.ScenarioProgressResponse
import ir.speaking.feature.scenario.scenario.dto.response.ScenarioResponse
import ir.speaking.feature.scenario.task.dto.ScenarioTaskResponse


fun Challenge.toResponse(fullImagePath: (String?) -> String?): ScenarioResponse = ScenarioResponse(
    id = this.uid.toString(),
    title = this.title,
    description = this.description,
    imageUrl = fullImagePath(imageUrl),
    aiName = this.aiName,
    aiAvatar = fullImagePath(aiAvatar),
    points = this.points,
    createdAt = this.createdAt.toString(),
    tasks = tasks.map { it.toResponse() },
    categoryId = "",
    persianTitle = this.persianTitle,
    persianDescription = this.persianDescription,
    gender = this.gender.name,
    starter = starter.name
)


fun ChallengeTask.toResponse(): ScenarioTaskResponse {
    return ScenarioTaskResponse(
        id = id.toString(),
        scenarioId = this.challengeId.toString(),
        description = description,
        persianDescription = persianDescription,
        createdAt = createdAt.toString()
    )
}

fun ChallengeProgress.toResponse() = ScenarioProgressResponse(
    id = uid.toString(),
    userId = userId.toString(),
    scenarioId = challengeId.toString(),
    score = score,
    completedAt = completedAt.toString()
)