package ir.speaking.feature.scenario.progress.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getUserUid
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.scenario.progress.dto.request.CreateScenarioProgressRequest
import ir.speaking.feature.scenario.progress.dto.request.UpdateProgressRequest
import ir.speaking.feature.scenario.progress.dto.response.toResponse
import ir.speaking.feature.scenario.progress.repository.ScenarioProgressRepository
import ir.speaking.feature.user.dto.response.toResponse
import ir.speaking.feature.user.repository.UserRepository
import org.koin.ktor.ext.inject

fun Application.scenarioProgressRouting() {
    val progressRepository by inject<ScenarioProgressRepository>()
    val userRepository by inject<UserRepository>()

    routing {
        route("/api/v1/scenario-progress") {
            getProgressById(progressRepository)
            authenticate(MyConstant.USER_JWT_NAME) {
                getUserProgress(progressRepository)
                createProgress(progressRepository, userRepository)
                updateProgress(progressRepository, userRepository)
            }
        }
    }
}

private fun Route.getProgressById(progressRepository: ScenarioProgressRepository) {
    get("/{id}") {
        val id = call.parameters["id"]?.toUUID() ?: throw AppException.BadRequest()
        val progress = progressRepository.getProgressById(id) ?: throw AppException.NotFound()
        call.successRespond(progress.toResponse())
    }
}

private fun Route.getUserProgress(progressRepository: ScenarioProgressRepository) {
    get {
        val userId = call.request.queryParameters["userId"]?.toUUID()
            ?: throw AppException.BadRequest()
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

        val pagedProgress = progressRepository.getProgressByUser(userId, page, size)
        call.successRespond(
            data = pagedProgress.items,
            pagingMeta = pagedProgress.pagingMeta
        )
    }
}

private fun Route.createProgress(progressRepository: ScenarioProgressRepository, userRepository: UserRepository) {
    post {
        val userId = call.getUserUid()

        val user = userRepository.getUserById(userId) ?: throw AppException.UnauthorizedAccess()
        val request = call.receive<CreateScenarioProgressRequest>()

        val progress = progressRepository.getProgressByUserAndScenario(userId, request.scenarioId.toUUID())

        if (progress != null && request.score == progress.score) {
            progressRepository.updateProgress(request.toProgress(userId))
            call.successRespond(user.toResponse())
        } else {
            progressRepository.createProgress(request.toProgress(userId))
            call.successRespond(user.copy(score = request.score + user.score).toResponse())
        }
    }
}

private fun Route.updateProgress(progressRepository: ScenarioProgressRepository, userRepository: UserRepository) {
    put {
        val uid = call.principal<JWTPrincipal>()!!.payload.getClaim("uid").asString().toUUID()
        userRepository.getUserById(uid) ?: throw AppException.UnauthorizedAccess()

        val request = call.receive<UpdateProgressRequest>()
        val progress = progressRepository.updateProgress(request.toProgress(uid))
            ?: throw AppException.NotFound()
        call.successRespond(progress.toResponse())
    }
}

