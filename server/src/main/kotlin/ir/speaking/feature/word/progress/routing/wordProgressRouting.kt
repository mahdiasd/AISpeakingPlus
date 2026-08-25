package ir.speaking.feature.word.progress.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getUserUid
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.user.repository.UserRepository
import ir.speaking.feature.word.progress.dto.WordProgressRequest
import ir.speaking.feature.word.progress.dto.toResponse
import ir.speaking.feature.word.progress.repository.WordProgressRepository
import org.koin.ktor.ext.inject

fun Application.wordProgressRouting() {
    val wordProgressRepository by inject<WordProgressRepository>()
    val userRepository by inject<UserRepository>()

    routing {
        route("/api/v1/word/progress") {
            authenticate(MyConstant.USER_JWT_NAME) {
                create(wordProgressRepository, userRepository)
            }
        }
    }
}


private fun Route.create(repo: WordProgressRepository, userRepository: UserRepository) {
    post {
        val userId = call.getUserUid()
        val request = call.receive<WordProgressRequest>()

        val user = userRepository.getUserById(userId) ?: throw AppException.UnauthorizedAccess()

        val score = if (request.isCorrect) request.wordScore else 0

        val word = repo.create(
            userId = userId,
            wordId = request.wordId.toUUID(),
            selectedOptionIndex = request.selectedOptionIndex,
            isCorrect = request.isCorrect,
            score = score
        )

        if (score > 0) {
            userRepository.updateUser(user.copy(score = user.score + score))
        }

        call.successRespond(word.toResponse())
    }
}



