package ir.speaking.feature.stage.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.FailureResponse
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.feature.stage.dto.HintRequest
import ir.speaking.feature.stage.dto.HintResponse
import ir.speaking.feature.stage.dto.StageCatalogResponse
import ir.speaking.feature.stage.dto.StageDetailResponse
import ir.speaking.feature.stage.repository.StageRepository
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.util.*

@Serializable
data class StageErrorResponse(
    val status: Int,
    val message: String,
    val code: String
)

@OptIn(ExperimentalKtorApi::class)
fun Application.stageRouting() {
    val stageRepository by inject<StageRepository>()

    routing {
        route("/api/v2/stages") {
            // Optional auth for catalog: allows guest or authenticated user
            authenticate(MyConstant.USER_JWT_NAME, optional = true) {
                get {
                    val principal = call.principal<JWTPrincipal>()
                    val uidString = principal?.payload?.getClaim("uid")?.asString()
                    val userId = uidString?.let { try { UUID.fromString(it) } catch (_: Exception) { null } }

                    val catalog = stageRepository.getStageCatalog(userId)
                    call.successRespond(catalog, message = "Stages retrieved successfully")
                }.describe {
                    tag("Stages")
                    summary = "List Stages"
                    description = "Get full story stages catalog with lock status and user progress"
                    responses {
                        HttpStatusCode.OK {
                            description = "Stages catalog retrieved successfully"
                            schema = jsonSchema<SuccessResponse<StageCatalogResponse>>()
                        }
                        HttpStatusCode.Unauthorized {
                            description = "Authentication required"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.InternalServerError {
                            description = "Internal server error"
                            schema = jsonSchema<FailureResponse>()
                        }
                    }
                }

                get("/{stageId}") {
                    val stageId = call.parameters["stageId"]
                    if (stageId.isNullOrBlank()) {
                        call.failureRespond(HttpStatusCode.BadRequest, "Stage ID is required")
                        return@get
                    }

                    val principal = call.principal<JWTPrincipal>()
                    val uidString = principal?.payload?.getClaim("uid")?.asString()
                    val userId = uidString?.let { try { UUID.fromString(it) } catch (_: Exception) { null } }

                    val stage = stageRepository.getStageDetail(stageId, userId)
                    if (stage == null) {
                        call.failureRespond(HttpStatusCode.NotFound, "Stage not found")
                        return@get
                    }

                    // Enforce gating
                    if (stage.orderIndex == 2 && userId == null) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            StageErrorResponse(
                                status = 401,
                                message = "ثبت‌نام برای ورود به مرحله ۲ الزامی است.",
                                code = "AUTH_REQUIRED"
                            )
                        )
                        return@get
                    }

                    if (stage.orderIndex >= 3) {
                        if (userId == null) {
                            call.respond(
                                HttpStatusCode.Unauthorized,
                                StageErrorResponse(
                                    status = 401,
                                    message = "ورود به حساب کاربری الزامی است.",
                                    code = "AUTH_REQUIRED"
                                )
                            )
                            return@get
                        }
                        if (stage.lockStatus == "LOCKED_SUBSCRIPTION") {
                            call.respond(
                                HttpStatusCode.PaymentRequired,
                                StageErrorResponse(
                                    status = 402,
                                    message = "برای دسترسی به مرحله ۳ به بعد، اشتراک ویژه تهیه کنید.",
                                    code = "SUBSCRIPTION_REQUIRED"
                                )
                            )
                            return@get
                        }
                    }

                    call.successRespond(stage, message = "Stage details retrieved")
                }.describe {
                    tag("Stages")
                    summary = "Get Stage Detail"
                    description = "Get stage details including objectives, characters, and conversation limits"
                    parameters {
                        path("stageId") {
                            description = "Unique stage identifier (e.g. stage-01-tehran-departure)"
                            required = true
                        }
                    }
                    responses {
                        HttpStatusCode.OK {
                            description = "Stage details retrieved successfully"
                            schema = jsonSchema<SuccessResponse<StageDetailResponse>>()
                        }
                        HttpStatusCode.BadRequest {
                            description = "Missing or invalid stage ID"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.Unauthorized {
                            description = "Authentication required for this stage"
                            schema = jsonSchema<StageErrorResponse>()
                        }
                        HttpStatusCode.PaymentRequired {
                            description = "Subscription required for this stage"
                            schema = jsonSchema<StageErrorResponse>()
                        }
                        HttpStatusCode.NotFound {
                            description = "Stage not found"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.InternalServerError {
                            description = "Internal server error"
                            schema = jsonSchema<FailureResponse>()
                        }
                    }
                }

                post("/{stageId}/hint") {
                    val stageId = call.parameters["stageId"]
                    if (stageId.isNullOrBlank()) {
                        call.failureRespond(HttpStatusCode.BadRequest, "Stage ID is required")
                        return@post
                    }

                    val stage = stageRepository.getStageDetail(stageId, null)
                    val hint = when (stage?.orderIndex) {
                        1 -> ir.speaking.feature.stage.dto.HintResponse(
                            suggestionEn = "I would like to check in my bags and request a window seat, please.",
                            explanationFa = "می‌خواهم بارم را تحویل دهم و یک صندلی کنار پنجره درخواست کنم."
                        )
                        2 -> ir.speaking.feature.stage.dto.HintResponse(
                            suggestionEn = "I will have the chicken with rice, and could you help me with the landing card?",
                            explanationFa = "من مرغ با برنج می‌خواهم، و آیا می‌توانید در مورد کارت ورود کمکم کنید؟"
                        )
                        else -> ir.speaking.feature.stage.dto.HintResponse(
                            suggestionEn = "Could you please explain what I need to do next?",
                            explanationFa = "می‌شود لطفاً توضیح دهید کار بعدی که باید انجام دهم چیست؟"
                        )
                    }

                    call.successRespond(hint, message = "Hint generated successfully")
                }.describe {
                    tag("Stages")
                    summary = "Get Stage Hint"
                    description = "Generate conversational English suggestion and Persian explanation based on stage history"
                    parameters {
                        path("stageId") {
                            description = "Unique stage identifier"
                            required = true
                        }
                    }
                    requestBody {
                        description = "Conversation message history (optional)"
                        required = false
                        schema = jsonSchema<HintRequest>()
                    }
                    responses {
                        HttpStatusCode.OK {
                            description = "Hint generated successfully"
                            schema = jsonSchema<SuccessResponse<HintResponse>>()
                        }
                        HttpStatusCode.BadRequest {
                            description = "Invalid stage ID"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.Unauthorized {
                            description = "Authentication required"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.NotFound {
                            description = "Stage not found"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.InternalServerError {
                            description = "Internal server error"
                            schema = jsonSchema<FailureResponse>()
                        }
                    }
                }
            }
        }
    }
}
