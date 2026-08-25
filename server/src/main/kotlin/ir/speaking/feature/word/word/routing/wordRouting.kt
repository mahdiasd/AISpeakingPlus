package ir.speaking.feature.word.word.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getUserUidOrNull
import ir.speaking.feature.word.progress.dto.toResponse
import ir.speaking.feature.word.progress.model.WordProgress
import ir.speaking.feature.word.progress.repository.WordProgressRepository
import ir.speaking.feature.word.word.dto.CrudWordRequest
import ir.speaking.feature.word.word.dto.toResponse
import ir.speaking.feature.word.word.model.DailyWord
import ir.speaking.feature.word.word.repository.WordRepository
import org.koin.ktor.ext.inject

fun Application.wordRouting() {
    val wordRepository by inject<WordRepository>()
    val wordProgressRepository by inject<WordProgressRepository>()

    routing {
        route("/api/v1/word") {
            authenticate(MyConstant.USER_JWT_NAME, optional = true) {
                getLastActive(wordRepository, wordProgressRepository)
            }

            authenticate(MyConstant.ADMIN_JWT_NAME) {
                create(wordRepository)
            }
        }
    }
}

private fun Route.getLastActive(
    wordRepository: WordRepository,
    wordProgressRepository: WordProgressRepository
) {
    get {
        val word = wordRepository.getLastActive() ?: wordRepository.getLast() ?: throw AppException.Gone()
        var wordProgress: WordProgress? = null
        call.getUserUidOrNull()?.let { userId ->
            wordProgressRepository.findByUserId(userId, word.uid)?.let {
                wordProgress = it
            }
        }
        call.successRespond(word.toResponse(wordProgress?.toResponse()))
    }
}


private fun Route.create(wordRepository: WordRepository) {
    post {
        val requests = call.receive<List<CrudWordRequest>>()
        val words = mutableListOf<DailyWord>()

        requests.forEach { request ->
            words.add(wordRepository.create(request))
        }

        call.successRespond(words.map { it.toResponse() })
    }
}
