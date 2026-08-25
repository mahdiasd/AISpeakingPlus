package ir.speaking.feature.lightener.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.core.exeptions.ErrorMessage
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getUserUid
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.lightener.dto.UpsertTranslationRequest
import ir.speaking.feature.lightener.dto.toResponse
import ir.speaking.feature.lightener.repository.TranslationRepository
import org.koin.ktor.ext.inject

fun Application.configureTranslationRouting() { // Renamed for clarity

    val repo by inject<TranslationRepository>()

    routing {
        route("/api/v1/translation") { // Use consistent naming "lightener"
            // Apply authentication to all routes within this block
            authenticate(MyConstant.USER_JWT_NAME) {
                // Define routes using the helper functions
                getPagedTranslationRoute(repo)
                createTranslationRoute(repo)
                updateTranslationRoute(repo)
                deleteTranslationRoute(repo) // Added delete route
            }
        }
    }
}

private fun Route.getPagedTranslationRoute(repo: TranslationRepository) {
    get {
        val userId = call.getUserUid()

        val page = call.request.queryParameters["page"]?.toIntOrNull()?.takeIf { it > 0 } ?: 1
        val size =
            call.request.queryParameters["size"]?.toIntOrNull()?.takeIf { it > 0 } ?: 10

        val searchText = call.request.queryParameters["searchText"]

        val pagedResult = repo.getPagedTranslation(
            userId = userId,
            page = page,
            pageSize = size,
            searchText = searchText
        )

        call.successRespond(
            data = pagedResult.items.map { it.toResponse() },
            pagingMeta = pagedResult.pagingMeta
        )
    }
}

private fun Route.createTranslationRoute(repo: TranslationRepository) {
    post {
        val userId = call.getUserUid()
        val request = call.receive<UpsertTranslationRequest>()
        val dbTranslation = repo.getBySourceText(request.sourceText, userId)

        if (dbTranslation == null) {
            val lightener = repo.create(userId, request)
            call.successRespond(lightener.toResponse())
        }else {
            call.failureRespond(HttpStatusCode.Conflict , ErrorMessage.CONFLICT_TRANSLATION)
        }

    }
}

private fun Route.updateTranslationRoute(repo: TranslationRepository) {
    put {
        val userId = call.getUserUid()

        val request = call.receive<UpsertTranslationRequest>()

        val updatedTranslation = repo.update(userId, request)

        if (updatedTranslation != null) {
            call.successRespond(data = updatedTranslation.toResponse())
        } else {
            call.failureRespond(
                httpStatusCode = HttpStatusCode.NotFound,
                message = "Translation with id '${request.uid}' not found or update failed."
            )
        }
    }
}

private fun Route.deleteTranslationRoute(repo: TranslationRepository) {
    delete("/{id}") {
        val userId = call.getUserUid()
        val lightenerId = call.parameters["id"].toUUID()

        val deleted = repo.deleteTranslation(userId, lightenerId)

        if (deleted) {
            call.successRespond(data = deleted)
        } else {
            // Not found or user doesn't have permission (repo returns false)
            call.failureRespond(
                HttpStatusCode.NotFound,
                "Translation with id '$lightenerId' not found or could not be deleted."
            )
        }
    }
}