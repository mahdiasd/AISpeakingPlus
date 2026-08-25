package ir.speaking.feature.challenge.progress.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getUserUid
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.challenge.challenge.dto.CreateChallengeProgressRequest
import ir.speaking.feature.challenge.progress.repository.ChallengeProgressRepository
import ir.speaking.feature.user.dto.response.toResponse
import ir.speaking.feature.user.repository.UserRepository
import org.koin.ktor.ext.inject

fun Application.challengeProgressRouting() {
    val progressRepository by inject<ChallengeProgressRepository>()
    val userRepository by inject<UserRepository>()

    routing {
        route("/api/v1/challenge-progress") {
            authenticate(MyConstant.USER_JWT_NAME) {
                createProgress(progressRepository, userRepository)
            }
        }
    }

}

private fun Route.createProgress(
    progressRepository: ChallengeProgressRepository,
    userRepository: UserRepository
) {
    post {
        val userId = call.getUserUid()

        val user = userRepository.getUserById(userId) ?: throw AppException.UnauthorizedAccess()
        val request = call.receive<CreateChallengeProgressRequest>()

        val progress = progressRepository.getProgressByUserAndChallenge(userId, request.challengeId.toUUID())

        if (progress != null && request.score == progress.score) {
            progressRepository.update(request.toProgress(userId))
            call.successRespond(user.toResponse())
        } else {
            progressRepository.create(request.toProgress(userId))
            call.successRespond(user.copy(score = request.score + user.score).toResponse())
        }
    }
}
