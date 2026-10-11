package ir.speaking.feature.stage_progress.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.FailureResponse
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.feature.stage.repository.StageRepository
import ir.speaking.feature.stage_progress.dto.EvaluationRequest
import ir.speaking.feature.stage_progress.dto.EvaluationResponse
import ir.speaking.feature.stage_progress.dto.GrammarErrorItem
import ir.speaking.feature.stage_progress.dto.SyncProgressRequest
import ir.speaking.feature.stage_progress.dto.SyncProgressResponse
import ir.speaking.feature.stage_progress.repository.StageProgressRepo
import ir.speaking.feature.stage_progress.service.EvaluationRubricService
import ir.speaking.feature.subscription.interceptor.enforceStageAccess
import org.koin.ktor.ext.inject
import java.util.*

@OptIn(ExperimentalKtorApi::class)
fun Application.progressRouting() {
    val rubricService by inject<EvaluationRubricService>()
    val progressRepo by inject<StageProgressRepo>()
    val stageRepository by inject<StageRepository>()

    routing {
        route("/api/v2/stages/{stageId}/evaluate") {
            authenticate(MyConstant.USER_JWT_NAME, optional = true) {
                post {
                    val stageId = call.parameters["stageId"]
                    if (stageId.isNullOrBlank()) {
                        call.failureRespond(HttpStatusCode.BadRequest, "Stage ID is required")
                        return@post
                    }

                    val principal = call.principal<JWTPrincipal>()
                    val uidString = principal?.payload?.getClaim("uid")?.asString()
                    val userId = uidString?.let { try { UUID.fromString(it) } catch (_: Exception) { null } }

                    val stage = stageRepository.getStageDetail(stageId, userId)
                    if (!call.enforceStageAccess(stage, userId)) return@post

                    val request = try {
                        call.receive<EvaluationRequest>()
                    } catch (_: Exception) {
                        EvaluationRequest()
                    }

                    // Pure AI grammar errors collected during conversation turns (no manual regex/heuristic matching)
                    val grammarErrors = request.grammarErrors
                    val totalGrammarErrors = maxOf(grammarErrors.size, request.grammarErrorsCount)
                    val objectiveCompleted = request.objectiveCompleted

                    val rubricResult = rubricService.calculateRubric(
                        grammarErrorsCount = totalGrammarErrors,
                        hintsUsedCount = request.hintsUsedCount,
                        objectiveCompleted = objectiveCompleted
                    )

                    var isHighScore = true
                    if (userId != null) {
                        val saveResult = progressRepo.saveOrUpdateProgress(
                            userId = userId,
                            stageId = stageId,
                            stars = rubricResult.starsEarned,
                            score = rubricResult.score
                        )
                        isHighScore = saveResult.isHighScore
                    }

                    val feedback = when {
                        !objectiveCompleted -> "مکالمه قبل از تکمیل هدف ماموریت پایان یافت؛ بنابراین ۰ ستاره به این تلاش تعلق گرفت. برای کسب ستاره، ماموریت مرحله را کامل کنید."
                        rubricResult.starsEarned == 3 -> "فوق‌العاده بود! ماموریت را بدون خطا و بدون راهنما به پایان رساندید و ۳ ستاره کامل کسب کردید."
                        rubricResult.starsEarned > 0 -> "هدف ماموریت با موفقیت انجام شد! برای کسب ۳ ستاره تلاش کنید بدون راهنما و با اصلاح خطاهای گرامری مرحله را تکرار کنید."
                        else -> "هدف ماموریت انجام شد اما به دلیل تعداد خطاها یا راهنماها ستاره‌ای کسب نشد. دوباره تلاش کنید!"
                    }

                    val response = EvaluationResponse(
                        stageId = stageId,
                        objectiveCompleted = objectiveCompleted,
                        grammarErrorsCount = totalGrammarErrors,
                        hintsUsedCount = request.hintsUsedCount,
                        totalPenalties = rubricResult.totalPenalties,
                        starsEarned = rubricResult.starsEarned,
                        score = rubricResult.score,
                        isHighScore = isHighScore,
                        unlockedNextStage = rubricResult.starsEarned >= 1,
                        grammarErrors = grammarErrors,
                        feedbackFa = feedback
                    )

                    call.successRespond(response, message = "Evaluation completed")
                }.describe {
                    tag("Progress")
                    summary = "Evaluate Stage"
                    description = "Score conversation transcript, detect grammar errors, and award stars"
                    parameters {
                        path("stageId") {
                            description = "Unique stage identifier"
                            required = true
                        }
                    }
                    requestBody {
                        description = "User performance including hints used, transcript, and turns"
                        required = true
                        schema = jsonSchema<EvaluationRequest>()
                    }
                    responses {
                        HttpStatusCode.OK {
                            description = "Evaluation completed successfully"
                            schema = jsonSchema<SuccessResponse<EvaluationResponse>>()
                        }
                        HttpStatusCode.BadRequest {
                            description = "Invalid stage ID or request body"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.Unauthorized {
                            description = "Authentication required"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.InternalServerError {
                            description = "Internal server error during evaluation"
                            schema = jsonSchema<FailureResponse>()
                        }
                    }
                }
            }
        }

        route("/api/v2/progress/sync") {
            authenticate(MyConstant.USER_JWT_NAME) {
                post {
                    val principal = call.principal<JWTPrincipal>()
                    val uidString = principal?.payload?.getClaim("uid")?.asString()
                    val userId = uidString?.let { try { UUID.fromString(it) } catch (_: Exception) { null } }

                    if (userId == null) {
                        call.failureRespond(HttpStatusCode.Unauthorized, "User authentication required")
                        return@post
                    }

                    val request = try {
                        call.receive<SyncProgressRequest>()
                    } catch (_: Exception) {
                        SyncProgressRequest()
                    }

                    val results = progressRepo.syncProgress(userId, request.items)
                    call.successRespond(
                        SyncProgressResponse(results),
                        message = "Progress synced successfully"
                    )
                }.describe {
                    tag("Progress")
                    summary = "Sync Offline Progress"
                    description = "Batch sync offline completed stage progress records with server"
                    requestBody {
                        description = "List of offline progress records to sync"
                        required = true
                        schema = jsonSchema<SyncProgressRequest>()
                    }
                    responses {
                        HttpStatusCode.OK {
                            description = "Progress records synced successfully"
                            schema = jsonSchema<SuccessResponse<SyncProgressResponse>>()
                        }
                        HttpStatusCode.BadRequest {
                            description = "Invalid request format"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.Unauthorized {
                            description = "Authentication required"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.InternalServerError {
                            description = "Server error while syncing progress"
                            schema = jsonSchema<FailureResponse>()
                        }
                    }
                }
            }
        }
    }
}
