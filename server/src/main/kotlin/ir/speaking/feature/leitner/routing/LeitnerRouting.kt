package ir.speaking.feature.leitner.routing

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
import ir.speaking.feature.leitner.dto.AddFlashcardRequest
import ir.speaking.feature.leitner.dto.LeitnerStatsResponse
import ir.speaking.feature.leitner.dto.ReminderTimeResponse
import ir.speaking.feature.leitner.dto.ReviewCardRequest
import ir.speaking.feature.leitner.dto.SetReminderTimeRequest
import ir.speaking.feature.leitner.model.UserFlashcard
import ir.speaking.feature.leitner.service.LeitnerService
import org.koin.ktor.ext.inject
import java.util.UUID

@OptIn(ExperimentalKtorApi::class)
fun Application.leitnerRouting() {
    val leitnerService by inject<LeitnerService>()

    routing {
        // v2 Primary route
        route("/api/v2/leitner") {
            registerLeitnerEndpoints(leitnerService, tag = "Leitner", suffix = "")
        }

        // v1 / Root API route for direct compatibility
        route("/api/leitner") {
            registerLeitnerEndpoints(leitnerService, tag = "Leitner", suffix = " (Legacy)")
        }
    }
}

@OptIn(ExperimentalKtorApi::class)
private fun Route.registerLeitnerEndpoints(
    leitnerService: LeitnerService,
    tag: String,
    suffix: String
) {
    authenticate(MyConstant.USER_JWT_NAME) {
        // 1. Get cards (filtered by box or dueOnly)
        get("/cards") {
            val userId = try {
                call.getUserUid()
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.Unauthorized, "Authentication required")
                return@get
            }

            val box = call.request.queryParameters["box"]?.toIntOrNull()
            val dueOnly = call.request.queryParameters["dueOnly"]?.toBooleanStrictOrNull() ?: false

            val cards = leitnerService.getCards(userId, box, dueOnly)
            call.successRespond(cards, message = "Flashcards retrieved successfully")
        }.describe {
            this.tag(tag)
            summary = "Get Flashcards$suffix"
            description = "Get user flashcards optionally filtered by box number or due for review status"
            parameters {
                query("box") {
                    description = "Optional Leitner box number (1 to 5)"
                    required = false
                }
                query("dueOnly") {
                    description = "Filter cards due for review today (true/false)"
                    required = false
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "Flashcards retrieved successfully"
                    schema = jsonSchema<SuccessResponse<List<UserFlashcard>>>()
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

        // 2. Add flashcard to deck
        post("/cards") {
            val userId = try {
                call.getUserUid()
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.Unauthorized, "Authentication required")
                return@post
            }

            val request = try {
                call.receive<AddFlashcardRequest>()
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
                return@post
            }

            try {
                val card = leitnerService.addCard(userId, request)
                call.successRespond(card, message = "Flashcard added successfully")
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.InternalServerError, "Failed to add flashcard: ${e.message}")
            }
        }.describe {
            this.tag(tag)
            summary = "Add Flashcard$suffix"
            description = "Add a word from lexicon or a custom flashcard to user Leitner deck"
            requestBody {
                description = "Flashcard details with lexiconId or custom word/meaning"
                required = true
                schema = jsonSchema<AddFlashcardRequest>()
            }
            responses {
                HttpStatusCode.OK {
                    description = "Flashcard created"
                    schema = jsonSchema<SuccessResponse<UserFlashcard>>()
                }
                HttpStatusCode.BadRequest {
                    description = "Invalid request body"
                    schema = jsonSchema<FailureResponse>()
                }
                HttpStatusCode.Unauthorized {
                    description = "Authentication required"
                    schema = jsonSchema<FailureResponse>()
                }
            }
        }

        // 3. Review a flashcard
        post("/review") {
            val userId = try {
                call.getUserUid()
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.Unauthorized, "Authentication required")
                return@post
            }

            val request = try {
                call.receive<ReviewCardRequest>()
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
                return@post
            }

            val cardUuid = try {
                UUID.fromString(request.cardId)
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.BadRequest, "Invalid cardId UUID")
                return@post
            }

            val updated = leitnerService.reviewCard(userId, cardUuid, request.remembered)
            if (updated != null) {
                call.successRespond(updated, message = "Review recorded successfully")
            } else {
                call.failureRespond(HttpStatusCode.NotFound, "Flashcard not found")
            }
        }.describe {
            this.tag(tag)
            summary = "Review Flashcard$suffix"
            description = "Record spaced repetition review result (advances 1 box on success, drops 1 box on failure)"
            requestBody {
                description = "Card review result with cardId and remembered boolean"
                required = true
                schema = jsonSchema<ReviewCardRequest>()
            }
            responses {
                HttpStatusCode.OK {
                    description = "Review recorded and card updated"
                    schema = jsonSchema<SuccessResponse<UserFlashcard>>()
                }
                HttpStatusCode.BadRequest {
                    description = "Invalid cardId or request"
                    schema = jsonSchema<FailureResponse>()
                }
                HttpStatusCode.NotFound {
                    description = "Flashcard not found"
                    schema = jsonSchema<FailureResponse>()
                }
                HttpStatusCode.Unauthorized {
                    description = "Authentication required"
                    schema = jsonSchema<FailureResponse>()
                }
            }
        }

        // 4. Update reminder time
        post("/reminder-time") {
            val userId = try {
                call.getUserUid()
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.Unauthorized, "Authentication required")
                return@post
            }

            val request = try {
                call.receive<SetReminderTimeRequest>()
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
                return@post
            }

            val time = if (!request.reminderTime.isNullOrBlank()) {
                request.reminderTime.trim()
            } else if (request.hour != null && request.minute != null) {
                "%02d:%02d".format(request.hour.coerceIn(0, 23), request.minute.coerceIn(0, 59))
            } else {
                "20:00"
            }

            val enabled = request.enabled ?: true
            val response = leitnerService.setReminderTime(userId, time, enabled)
            call.successRespond(response, message = "Reminder time updated successfully")
        }.describe {
            this.tag(tag)
            summary = "Set Reminder Time$suffix"
            description = "Set daily study reminder time and enabled status for Leitner notifications"
            requestBody {
                description = "Reminder settings (reminderTime string e.g. '20:00' or hour/minute ints)"
                required = true
                schema = jsonSchema<SetReminderTimeRequest>()
            }
            responses {
                HttpStatusCode.OK {
                    description = "Reminder time updated"
                    schema = jsonSchema<SuccessResponse<ReminderTimeResponse>>()
                }
                HttpStatusCode.BadRequest {
                    description = "Invalid request parameters"
                    schema = jsonSchema<FailureResponse>()
                }
                HttpStatusCode.Unauthorized {
                    description = "Authentication required"
                    schema = jsonSchema<FailureResponse>()
                }
            }
        }

        // 5. Get reminder time
        get("/reminder-time") {
            val userId = try {
                call.getUserUid()
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.Unauthorized, "Authentication required")
                return@get
            }

            val response = leitnerService.getReminderTime(userId)
            call.successRespond(response, message = "Reminder time retrieved successfully")
        }.describe {
            this.tag(tag)
            summary = "Get Reminder Time$suffix"
            description = "Get configured daily study reminder time and enabled status"
            responses {
                HttpStatusCode.OK {
                    description = "Reminder time retrieved"
                    schema = jsonSchema<SuccessResponse<ReminderTimeResponse>>()
                }
                HttpStatusCode.Unauthorized {
                    description = "Authentication required"
                    schema = jsonSchema<FailureResponse>()
                }
            }
        }

        // 6. Get Leitner statistics
        get("/stats") {
            val userId = try {
                call.getUserUid()
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.Unauthorized, "Authentication required")
                return@get
            }

            val stats = leitnerService.getStats(userId)
            call.successRespond(stats, message = "Leitner statistics retrieved successfully")
        }.describe {
            this.tag(tag)
            summary = "Get Leitner Stats$suffix"
            description = "Get total cards count, due cards count, and counts per box (1 to 5)"
            responses {
                HttpStatusCode.OK {
                    description = "Statistics retrieved"
                    schema = jsonSchema<SuccessResponse<LeitnerStatsResponse>>()
                }
                HttpStatusCode.Unauthorized {
                    description = "Authentication required"
                    schema = jsonSchema<FailureResponse>()
                }
            }
        }

        // 7. Delete a flashcard
        delete("/cards/{cardId}") {
            val userId = try {
                call.getUserUid()
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.Unauthorized, "Authentication required")
                return@delete
            }

            val cardIdStr = call.parameters["cardId"]
            val cardUuid = try {
                UUID.fromString(cardIdStr)
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.BadRequest, "Invalid cardId UUID")
                return@delete
            }

            val deleted = leitnerService.deleteCard(userId, cardUuid)
            if (deleted) {
                call.successRespond(true, message = "Flashcard deleted successfully")
            } else {
                call.failureRespond(HttpStatusCode.NotFound, "Flashcard not found")
            }
        }.describe {
            this.tag(tag)
            summary = "Delete Flashcard$suffix"
            description = "Remove a flashcard from user deck"
            parameters {
                path("cardId") {
                    description = "Flashcard UUID"
                    required = true
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "Flashcard deleted"
                    schema = jsonSchema<SuccessResponse<Boolean>>()
                }
                HttpStatusCode.NotFound {
                    description = "Flashcard not found"
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
