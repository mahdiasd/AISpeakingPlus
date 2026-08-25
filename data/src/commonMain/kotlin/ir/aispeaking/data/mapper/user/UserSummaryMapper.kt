package ir.aispeaking.data.mapper.user

import ir.aispeaking.domain.model.user.UserSummary
import ir.aispeaking.network.dto.user.UserSummaryResponse
import ir.aispeaking.storage.model.user.SharedPrefUserSummary

fun UserSummaryResponse.toDomain(): UserSummary {
    return UserSummary(
        uid = uid,
        score = score ?: 0,
        nickName = nickName ?: "",
        avatar = avatar ?: ""
    )
}

fun UserSummaryResponse.toSharedPref(): SharedPrefUserSummary {
    return SharedPrefUserSummary(
        uid = uid,
        score = score ?: 0,
        nickName = nickName ?: "",
        avatar = avatar ?: ""
    )
}

fun SharedPrefUserSummary.toDomain(): UserSummary {
    return UserSummary(
        uid = uid,
        score = score,
        nickName = nickName ?: "",
        avatar = avatar ?: ""
    )
}
