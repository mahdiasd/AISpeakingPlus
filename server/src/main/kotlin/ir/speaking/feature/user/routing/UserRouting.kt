package ir.speaking.feature.user.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.FailureResponse
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getUserUid
import ir.speaking.feature.user.dto.DetailedUserProfileResponse
import ir.speaking.feature.user.dto.UpdateProfileRequest
import ir.speaking.feature.user.repository.UserRepo
import org.koin.ktor.ext.inject

@OptIn(ExperimentalKtorApi::class)
fun Application.userRouting() {
    val userRepo by inject<UserRepo>()

    routing {
        route("/api/v2/user") {
            authenticate(MyConstant.USER_JWT_NAME) {
                get("/profile") {
                    val userId = try {
                        call.getUserUid()
                    } catch (e: Exception) {
                        call.failureRespond(HttpStatusCode.Unauthorized, "Authentication required")
                        return@get
                    }

                    val profile = userRepo.getDetailedUserProfile(userId)
                    if (profile != null) {
                        call.successRespond(profile, message = "User profile retrieved successfully")
                    } else {
                        call.failureRespond(HttpStatusCode.NotFound, "User not found")
                    }
                }.describe {
                    tag("User Profile")
                    summary = "Get User Profile"
                    description = "Get detailed profile information, stars, and subscription status for authenticated user"
                    responses {
                        HttpStatusCode.OK {
                            description = "User profile retrieved successfully"
                            schema = jsonSchema<SuccessResponse<DetailedUserProfileResponse>>()
                        }
                        HttpStatusCode.Unauthorized {
                            description = "Authentication required or invalid token"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.NotFound {
                            description = "User not found"
                            schema = jsonSchema<FailureResponse>()
                        }
                    }
                }

                put("/profile") {
                    val userId = try {
                        call.getUserUid()
                    } catch (e: Exception) {
                        call.failureRespond(HttpStatusCode.Unauthorized, "Authentication required")
                        return@put
                    }

                    val request = try {
                        call.receive<UpdateProfileRequest>()
                    } catch (e: Exception) {
                        call.failureRespond(HttpStatusCode.BadRequest, "Invalid request body format")
                        return@put
                    }

                    if (request.nickName != null) {
                        val trimmed = request.nickName.trim()
                        if (trimmed.length < 2 || trimmed.length > 50) {
                            call.failureRespond(HttpStatusCode.BadRequest, "نام مستعار باید بین ۲ تا ۵۰ نویسه باشد")
                            return@put
                        }
                    }

                    val updatedProfile = userRepo.updateUserProfile(userId, request)
                    if (updatedProfile != null) {
                        call.successRespond(updatedProfile, message = "Profile updated successfully")
                    } else {
                        call.failureRespond(HttpStatusCode.NotFound, "User not found")
                    }
                }.describe {
                    tag("User Profile")
                    summary = "Update User Profile"
                    description = "Update nickname, avatar, or name for authenticated user"
                    requestBody {
                        description = "Profile update payload (e.g. {\"nickName\": \"Reza\", \"avatar\": \"avatar_g5\"})"
                        required = true
                        schema = jsonSchema<UpdateProfileRequest>()
                    }
                    responses {
                        HttpStatusCode.OK {
                            description = "Profile updated successfully"
                            schema = jsonSchema<SuccessResponse<DetailedUserProfileResponse>>()
                        }
                        HttpStatusCode.BadRequest {
                            description = "Validation failed"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.Unauthorized {
                            description = "Authentication required"
                            schema = jsonSchema<FailureResponse>()
                        }
                    }
                }
            }
        }
    }
}
