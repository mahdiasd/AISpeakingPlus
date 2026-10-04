package ir.aispeaking.data.mapper.stage

import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stage.StageLockStatus
import ir.aispeaking.domain.model.stage.StageProgress
import ir.aispeaking.network.model.stage.dto.StageDetailDto
import ir.aispeaking.network.model.stage.dto.StageProgressDto
import ir.aispeaking.network.model.stage.dto.StageSummaryDto
import ir.aispeaking.network.resolveMediaUrl

fun StageSummaryDto.toDomain(): Stage {
    return Stage(
        id = id,
        orderIndex = orderIndex,
        title = title,
        titleFa = titleFa,
        briefing = "",
        briefingFa = briefingFa,
        targetObjective = "",
        targetObjectiveFa = targetObjectiveFa ?: "",
        characterBehavior = null,
        backgroundUrl = resolveMediaUrl(backgroundUrl),
        characterName = characterName,
        characterAvatarUrl = characterAvatarUrl?.let { resolveMediaUrl(it) },
        characterGender = characterGender,
        voiceId = null,
        initialSpeaker = initialSpeaker,
        maxTurns = maxTurns,
        lockStatus = try { StageLockStatus.valueOf(lockStatus) } catch (_: Exception) { StageLockStatus.UNLOCKED },
        userProgress = progress?.toDomain(id)
    )
}

fun StageDetailDto.toDomain(): Stage {
    return Stage(
        id = id,
        orderIndex = orderIndex,
        title = title,
        titleFa = titleFa,
        briefing = briefing,
        briefingFa = briefingFa,
        targetObjective = targetObjective,
        targetObjectiveFa = targetObjectiveFa,
        characterBehavior = characterBehavior,
        backgroundUrl = resolveMediaUrl(backgroundUrl),
        characterName = characterName,
        characterAvatarUrl = characterAvatarUrl?.let { resolveMediaUrl(it) },
        characterGender = characterGender,
        voiceId = voiceId,
        initialSpeaker = initialSpeaker,
        maxTurns = maxTurns,
        lockStatus = try { StageLockStatus.valueOf(lockStatus) } catch (_: Exception) { StageLockStatus.UNLOCKED },
        userProgress = progress?.toDomain(id)
    )
}

fun StageProgressDto.toDomain(stageId: String = ""): StageProgress {
    return StageProgress(
        id = "",
        userId = "",
        stageId = stageId,
        stars = stars,
        bestScore = bestScore,
        repeatCount = repeatCount,
        completedAt = completedAt,
        updatedAt = completedAt
    )
}
