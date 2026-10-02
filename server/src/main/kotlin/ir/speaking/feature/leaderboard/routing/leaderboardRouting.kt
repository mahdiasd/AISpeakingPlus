package ir.speaking.feature.leaderboard.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.FailureResponse
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.feature.leaderboard.dto.LeaderboardResponse
import ir.speaking.feature.leaderboard.repository.LeaderboardRepo
import org.koin.ktor.ext.inject
import java.util.*

@OptIn(ExperimentalKtorApi::class)
fun Application.leaderboardRouting() {
    val leaderboardRepo by inject<LeaderboardRepo>()

    routing {
        route("/api/v2/leaderboard") {
            authenticate(MyConstant.USER_JWT_NAME, optional = true) {
                get("/journey") {
                    val principal = call.principal<JWTPrincipal>()
                    val uidString = principal?.payload?.getClaim("uid")?.asString()
                    val userId = uidString?.let { try { UUID.fromString(it) } catch (_: Exception) { null } }

                    val page = call.parameters["page"]?.toIntOrNull() ?: 1
                    val pageSize = call.parameters["pageSize"]?.toIntOrNull() ?: 20

                    val response = leaderboardRepo.getLeaderboard(
                        currentUserId = userId,
                        page = page,
                        pageSize = pageSize
                    )

                    call.successRespond(response, message = "Leaderboard retrieved successfully")
                }.describe {
                    tag("Leaderboard")
                    summary = "Get Journey Leaderboard"
                    description = "Get user leaderboard ranked by total stars with current user rank and pagination"
                    parameters {
                        query("page") {
                            description = "Page number (default 1)"
                            required = false
                        }
                        query("pageSize") {
                            description = "Items per page (default 20)"
                            required = false
                        }
                    }
                    responses {
                        HttpStatusCode.OK {
                            description = "Leaderboard retrieved successfully"
                            schema = jsonSchema<SuccessResponse<LeaderboardResponse>>()
                        }
                        HttpStatusCode.BadRequest {
                            description = "Invalid query parameters"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.InternalServerError {
                            description = "Server error"
                            schema = jsonSchema<FailureResponse>()
                        }
                    }
                }
            }
        }
    }
}
