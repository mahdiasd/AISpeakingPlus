package ir.aispeaking.data.mapper.user

import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.domain.model.user.Gender
import ir.aispeaking.domain.model.user.RegisterParam
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.network.dto.user.CreateUserRequest
import ir.aispeaking.network.dto.user.UpdateUserRequest
import ir.aispeaking.network.dto.user.UserResponse
import ir.aispeaking.storage.model.user.SharedPrefUser

fun String?.toGender(): Gender? {
    return if (this.isNullOrEmpty()) null
    else {
        return if (this.equals("Man", true)) Gender.Man
        else if (this.equals("Woman", true)) Gender.Woman
        else null
    }
}

fun UserResponse.toDomain(): User {
    return User(
        uid = uid,
        nickName = nickName,
        firstName = firstName,
        lastName = lastName,
        mobile = mobile,
        gender = gender.toGender(),
        languageLevel = LanguageLevel.valueOf(languageLevel),
        age = age,
        score = score,
        avatar = avatar,
        completedScenarioCount = completedScenarioCount,
        completedWordCount = completedWordCount,
        completedChallengeCount = completedChallengeCount,
    )
}

fun User.toSharedPref(): SharedPrefUser {
    return SharedPrefUser(
        uid = uid,
        nickName = nickName,
        firstName = firstName,
        lastName = lastName,
        mobile = mobile,
        gender = gender?.name,
        languageLevel = languageLevel.name,
        age = age,
        score = score,
        avatar = avatar,
        completedScenarioCount = completedScenarioCount,
        completedWordCount = completedWordCount,
        completedChallengeCount = completedChallengeCount,
    )
}

fun SharedPrefUser.toDomain(): User {
    return User(
        uid = uid,
        nickName = nickName,
        firstName = firstName,
        lastName = lastName,
        mobile = mobile,
        gender = gender.toGender(),
        languageLevel = LanguageLevel.valueOf(languageLevel),
        age = age,
        score = score,
        avatar = avatar,
        completedScenarioCount = completedScenarioCount,
        completedWordCount = completedWordCount,
        completedChallengeCount = completedChallengeCount,
    )
}

fun RegisterParam.toCreateUserRequest(): CreateUserRequest {
    return CreateUserRequest(
        nickName = nickName,
        firstName = firstName,
        lastName = lastName,
        mobile = mobile,
        gender = gender?.name,
        languageLevel = languageLevel!!.name,
        avatar = avatar,
        age = age,
    )
}

fun User.toUpdateUserRequest(): UpdateUserRequest {
    return UpdateUserRequest(
        uid = uid,
        nickName = nickName,
        firstName = firstName,
        lastName = lastName,
        mobile = mobile,
        gender = gender?.name,
        languageLevel = languageLevel.name,
        age = age,
        avatar = avatar
    )
}
