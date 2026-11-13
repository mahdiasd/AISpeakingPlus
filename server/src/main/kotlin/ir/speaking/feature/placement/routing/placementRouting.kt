package ir.speaking.feature.placement.routing

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
import ir.speaking.feature.placement.dto.PlacementEvaluationResponseServerDto
import ir.speaking.feature.placement.dto.PlacementQuestionsResponseServerDto
import ir.speaking.feature.placement.dto.PlacementSubmitRequestServerDto
import ir.speaking.feature.placement.service.PlacementEvaluationService
import org.koin.ktor.ext.inject

@OptIn(ExperimentalKtorApi::class)
fun Application.placementRouting() {
    val placementService by inject<PlacementEvaluationService>()

    routing {
        route("/api/v2/placement") {
            authenticate(MyConstant.USER_JWT_NAME, optional = true) {
                // GET /api/v2/placement/questions
                get("/questions") {
                    val principal = call.principal<JWTPrincipal>()
                    val uid = principal?.payload?.getClaim("uid")?.asString() ?: "guest"

                    val response = placementService.generateSession(userId = uid)
                    call.successRespond(data = response)
                }.describe {
                    tag("Placement")
                    summary = "Get Placement Test Questions"
                    description = "Generates a 20-question stratified adaptive English placement test session (4 questions per CEFR band from A1 to C1)."
                    responses {
                        HttpStatusCode.OK {
                            description = "Randomized placement test questions without correct answer keys"
                            schema = jsonSchema<SuccessResponse<PlacementQuestionsResponseServerDto>>()
                        }
                        HttpStatusCode.InternalServerError {
                            description = "Server error while generating placement test questions"
                            schema = jsonSchema<FailureResponse>()
                        }
                    }
                }

                // POST /api/v2/placement/evaluate
                post("/evaluate") {
                    val principal = call.principal<JWTPrincipal>()
                    val uid = principal?.payload?.getClaim("uid")?.asString()

                    val submission = try {
                        call.receive<PlacementSubmitRequestServerDto>()
                    } catch (e: Exception) {
                        call.failureRespond(HttpStatusCode.BadRequest, "Invalid submission body: ${e.message}")
                        return@post
                    }

                    if (submission.sessionId.isBlank()) {
                        call.failureRespond(HttpStatusCode.BadRequest, "sessionId is required")
                        return@post
                    }

                    val result = placementService.evaluateSession(userId = uid, submission = submission)
                    call.successRespond(data = result)
                }.describe {
                    tag("Placement")
                    summary = "Evaluate Placement Test"
                    description = "Evaluates user submitted answers, computes CEFR level, updates user profile, and returns an itemized review report."
                    requestBody {
                        description = "Answers payload with session ID and question-to-option mappings"
                        required = true
                        schema = jsonSchema<PlacementSubmitRequestServerDto>()
                    }
                    responses {
                        HttpStatusCode.OK {
                            description = "Placement test evaluation result with score, CEFR level, and question review list"
                            schema = jsonSchema<SuccessResponse<PlacementEvaluationResponseServerDto>>()
                        }
                        HttpStatusCode.BadRequest {
                            description = "Invalid request payload or missing sessionId"
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
    }
}
