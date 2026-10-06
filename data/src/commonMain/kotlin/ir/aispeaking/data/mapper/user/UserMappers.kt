package ir.aispeaking.data.mapper.user

import ir.aispeaking.domain.model.user.User
import ir.aispeaking.network.model.auth.dto.UserProfileDto
import ir.aispeaking.storage.model.user.SharedPrefUser

fun UserProfileDto.toDomain(): User {
    return User(
        uid = id,
        nickName = nickName,
        firstName = "",
        lastName = "",
        mobile = phoneNumber,
        gender = null,
        languageLevel = "",
        score = score,
        avatar = avatar
    )
}

fun SharedPrefUser.toDomain(): User {
    return User(
        uid = uid,
        nickName = nickName,
        firstName = firstName,
        lastName = lastName,
        mobile = mobile,
        gender = gender,
        languageLevel = languageLevel,
        score = score,
        avatar = avatar
    )
}

fun User.toStorage(): SharedPrefUser {
    return SharedPrefUser(
        uid = uid,
        nickName = nickName,
        firstName = firstName,
        lastName = lastName,
        mobile = mobile,
        gender = gender,
        languageLevel = languageLevel,
        age = 0,
        score = score,
        avatar = avatar
    )
}
