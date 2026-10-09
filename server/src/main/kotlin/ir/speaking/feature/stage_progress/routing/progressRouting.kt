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
import ir.speaking.feature.stage.db.StageTable
import ir.speaking.feature.stage_progress.dto.EvaluationRequest
import ir.speaking.feature.stage_progress.dto.EvaluationResponse
import ir.speaking.feature.stage_progress.dto.GrammarErrorItem
import ir.speaking.feature.stage_progress.dto.SyncProgressRequest
import ir.speaking.feature.stage_progress.dto.SyncProgressResponse
import ir.speaking.feature.stage_progress.repository.StageProgressRepo
import ir.speaking.feature.stage_progress.service.EvaluationRubricService
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import java.util.*

@OptIn(ExperimentalKtorApi::class)
fun Application.progressRouting() {
    val rubricService by inject<EvaluationRubricService>()
    val progressRepo by inject<StageProgressRepo>()

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

                    val request = try {
                        call.receive<EvaluationRequest>()
                    } catch (_: Exception) {
                        EvaluationRequest()
                    }

                    // Heuristic grammar check on transcript if present, combined with client detected errors
                    val grammarErrors = mutableListOf<GrammarErrorItem>()
                    grammarErrors.addAll(request.grammarErrors)

                    for (item in request.transcript) {
                        if (item.role.equals("User", ignoreCase = true)) {
                            if (grammarErrors.any { it.original.equals(item.content, ignoreCase = true) }) {
                                continue
                            }
                            val text = item.content.lowercase()
                            val errorItem = when {
                                text.contains("i wants") -> GrammarErrorItem(
                                    original = item.content,
                                    correction = item.content.replace("i wants", "I want", ignoreCase = true),
                                    explanationFa = "اشکال در فاعل و فعل: برای ضمیر «I» از فعل ساده بدون s استفاده کنید: I want"
                                )
                                text.contains("i flying") || text.contains("i going") || text.contains("i travelling") || text.contains("i studying") -> GrammarErrorItem(
                                    original = item.content,
                                    correction = item.content
                                        .replace("i flying", "I am flying", ignoreCase = true)
                                        .replace("i going", "I am going", ignoreCase = true)
                                        .replace("i travelling", "I am travelling", ignoreCase = true)
                                        .replace("i studying", "I am studying", ignoreCase = true),
                                    explanationFa = "اشکال در زمان استمراری: بعد از «I» باید فعل کمکی «am» قرار گیرد: I am flying / I am going"
                                )
                                text.contains("he want ") || text.contains("she want ") -> GrammarErrorItem(
                                    original = item.content,
                                    correction = item.content
                                        .replace("he want ", "He wants ", ignoreCase = true)
                                        .replace("she want ", "She wants ", ignoreCase = true),
                                    explanationFa = "اشکال در سوم‌شخص: برای «he / she» فعل باید با s بیاید: He wants / She wants"
                                )
                                text.contains("they is") || text.contains("we is") -> GrammarErrorItem(
                                    original = item.content,
                                    correction = item.content
                                        .replace("they is", "They are", ignoreCase = true)
                                        .replace("we is", "We are", ignoreCase = true),
                                    explanationFa = "اشکال در تطابق فاعل و فعل: برای فاعل جمع از «are» استفاده کنید: They are / We are"
                                )
                                text.contains("i would to") || text.contains("would like to order of") -> GrammarErrorItem(
                                    original = item.content,
                                    correction = item.content
                                        .replace("i would to", "I would like to", ignoreCase = true)
                                        .replace("would like to order of", "would like to order", ignoreCase = true),
                                    explanationFa = "اشکال ساختار: بعد از «would like» شکل ساده فعل می‌آید: I would like to order"
                                )
                                text.contains("give me food") || text.contains("give me chicken") -> GrammarErrorItem(
                                    original = item.content,
                                    correction = item.content
                                        .replace("give me food", "I would like to have the food", ignoreCase = true)
                                        .replace("give me chicken", "I would like the chicken", ignoreCase = true),
                                    explanationFa = "نکته کاربردی: در زبان انگلیسی برای سفارش غذا بهتر است از عبارات مودبانه مثل «I would like...» یا «Could I please have...» استفاده کنید."
                                )
                                else -> null
                            }
                            if (errorItem != null) {
                                grammarErrors.add(errorItem)
                            }
                        }
                    }

                    val totalGrammarErrors = maxOf(grammarErrors.size, request.grammarErrorsCount)
                    val objectiveCompleted = request.turnsCount >= 2 || request.transcript.size >= 2
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

                    val feedback = if (rubricResult.starsEarned == 3) {
                        "فوق‌العاده بود! ماموریت را بدون خطا و بدون راهنما به پایان رساندید و ۳ ستاره کامل کسب کردید."
                    } else if (rubricResult.starsEarned > 0) {
                        "هدف ماموریت با موفقیت انجام شد! برای کسب ۳ ستاره تلاش کنید بدون راهنما و با اصلاح خطاهای گرامری مرحله را تکرار کنید."
                    } else {
                        "برای کسب ستاره، تلاش کنید هدف ماموریت را به طور کامل تکمیل کنید و از راهنماهای کمتری استفاده نمایید."
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
