package ir.speaking.admin.message

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.redis.message.MessageDto
import ir.speaking.core.redis.message.MessageRedisRepository
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.*
import kotlinx.serialization.SerializationException
import org.koin.ktor.ext.inject
import java.util.*

fun Application.adminMessageRouting() {
    val messageRedisRepository by inject<MessageRedisRepository>()

    routing {
        route("/api/v1/admin/welcome-message") {
            authenticate(MyConstant.ADMIN_JWT_NAME) {
                upsertMessage(messageRedisRepository)
                deleteMessage(messageRedisRepository)
            }
        }
    }
}

private fun Route.upsertMessage(repository: MessageRedisRepository) {
    post {
        try {
            val multipart = call.receiveMultipart()
            val request = parseMultipartData<MessageDto>(multipart)

            val oldMessage = repository.getMessage()

            val messageToSave = request.copy(
                id = oldMessage?.id ?: UUID.randomUUID().toString(),
                imageUrl = request.imageUrl ?: "",
                // این خط رو تغییر بده:
                message = request.message.replace("\\n", "\n").replace("/n", "\n")
            )

            repository.addMessage(messageToSave)

            call.successRespond(messageToSave.copy(
                imageUrl = call.getFullPath(messageToSave.imageUrl ?: "")
            ))

        } catch (e: Exception) {
            when (e) {
                is AppException -> throw e
                is SerializationException -> throw AppException.BadRequest("Invalid request data: ${e.message}")
                else -> {
                    println("Error upserting message: ${e.message}")
                    call.failureRespond(HttpStatusCode.InternalServerError, e.message ?: "Failed to update message")
                }
            }
        }
    }
}

private fun Route.deleteMessage(repository: MessageRedisRepository) {
    delete {
        try {
            val current = repository.getMessage()
            current?.imageUrl?.let { ImageSaver.deleteImage(it) }

            repository.clear()
            call.successRespond("پیام با موفقیت حذف شد")
        } catch (e: Exception) {
            throw AppException.UnknownError("خطا در حذف پیام")
        }
    }
}