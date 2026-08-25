package ir.speaking.admin.challenge.routing


import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getFullPath
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.challenge.challenge.dto.SaveChallengeRequest
import ir.speaking.feature.challenge.challenge.mapper.toResponse
import ir.speaking.feature.challenge.challenge.repository.ChallengeRepository
import org.koin.ktor.ext.inject

fun Application.adminChallengeRouting() {
    val challengeRepository by inject<ChallengeRepository>()
    routing {
        route("/api/v1/daily-challenge") {
            authenticate(MyConstant.ADMIN_JWT_NAME) {
                post {
                    val request = call.receive<SaveChallengeRequest>()
                    val challenge = challengeRepository.create(request)
                    call.successRespond(challenge.toResponse { call.getFullPath(it) })
                }

                put {
                    val request = call.receive<SaveChallengeRequest>()
                    val challenge = challengeRepository.update(request)
                        ?: throw AppException.NotFound("Daily challenge not found")
                    call.successRespond(challenge.toResponse { call.getFullPath(it) })
                }

                delete("{id}") {
                    val id = call.parameters["id"] ?: throw AppException.BadRequest("Missing id parameter")
                    val success = challengeRepository.delete(id.toUUID())
                    call.successRespond(mapOf("deleted" to success))
                }
            }
        }
    }
}