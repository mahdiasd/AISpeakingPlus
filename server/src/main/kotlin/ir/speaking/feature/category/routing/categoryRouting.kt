package ir.speaking.feature.category.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.getBaseUrl
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.category.dto.response.toResponse
import ir.speaking.feature.category.repository.CategoryRepository
import org.koin.ktor.ext.inject

fun Application.categoryRouting() {
    val categoryRepository by inject<CategoryRepository>()

    routing {
        route("/api/v1/categories") {
            getCategoryById(categoryRepository)
            getAllCategories(categoryRepository)
        }
    }
}

private fun Route.getAllCategories(categoryRepository: CategoryRepository) {
    get {
        val categories = categoryRepository.getAllCategories()
        call.successRespond(
            data = categories.map { it.toResponse(call.getBaseUrl()) }
        )
    }
}

private fun Route.getCategoryById(categoryRepository: CategoryRepository) {
    get("/{id}") {
        val id = call.parameters["id"]?.toUUID() ?: throw AppException.BadRequest()
        val category = categoryRepository.getCategoryById(id) ?: throw AppException.NotFound()
        call.successRespond(category.toResponse(call.getBaseUrl()))
    }
}
