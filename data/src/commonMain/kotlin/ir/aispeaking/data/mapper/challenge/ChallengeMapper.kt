package ir.aispeaking.data.mapper.challenge

import ir.aispeaking.domain.model.challenge.Challenge
import ir.aispeaking.domain.model.challenge.ChallengeDetail
import ir.aispeaking.domain.model.challenge.ChallengeProgress
import ir.aispeaking.domain.model.challenge.ChallengeSummary
import ir.aispeaking.domain.model.challenge.ChallengeTask
import ir.aispeaking.network.dto.challenge.ChallengeDetailResponse
import ir.aispeaking.network.dto.challenge.ChallengeProgressResponse
import ir.aispeaking.network.dto.challenge.ChallengeResponse
import ir.aispeaking.network.dto.challenge.ChallengeSummaryResponse
import ir.aispeaking.network.dto.challenge.ChallengeTaskResponse

import ir.aispeaking.utils.time.toInstantOrNull

fun ChallengeResponse.toDomain(): Challenge {
    return Challenge(
        uid = uid,
        title = title,
        description = description,
        imageUrl = imageUrl,
        aiName = aiName,
        aiAvatar = aiAvatar,
        points = points,
        createdAt = createdAt.toInstantOrNull(),
        tasks = tasks.map { it.toDomain() }
    )
}

fun ChallengeSummaryResponse.toDomain(): ChallengeSummary {
    return ChallengeSummary(
        id = id,
        title = title,
        imageUrl = imageUrl,
        description = description,
        aiAvatar = aiAvatar,
        score = score
    )
}

fun ChallengeTaskResponse.toDomain(): ChallengeTask {
    return ChallengeTask(
        id = id,
        description = description,
        persianDescription = persianDescription,
    )
}

fun ChallengeDetailResponse.toDomain(): ChallengeDetail {
    return ChallengeDetail(
        challenge = challenge.toDomain(),
        userHaveSubscription = userHaveSubscription,
        progress = progress?.toDomain()
    )
}

fun ChallengeProgressResponse.toDomain(): ChallengeProgress {
    return ChallengeProgress(
        uid = id,
        userId = userId,
        challengeId = challengeId,
        score = score,
        completedAt = completedAt.toInstantOrNull()
    )
}