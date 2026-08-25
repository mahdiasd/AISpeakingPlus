package ir.speaking.admin.category.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.admin.category.dto.CreateCategoryRequest
import ir.speaking.admin.category.dto.UpdateCategoryRequest
import ir.speaking.admin.category.dto.toCategory
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.message.SuccessMessage
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getBaseUrl
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.category.dto.response.toResponse
import ir.speaking.feature.category.repository.CategoryRepository
import org.koin.ktor.ext.inject

fun Application.adminCategoryRouting() {
    val categoryRepository by inject<CategoryRepository>()

    routing {
        route("/api/v1/admin/categories") {
            authenticate(MyConstant.ADMIN_JWT_NAME) {
                createCategory(categoryRepository)
                updateCategory(categoryRepository)
                deleteCategory(categoryRepository)
            }
        }
    }
}

private fun Route.createCategory(categoryRepository: CategoryRepository) {
    post {
        val request = call.receive<CreateCategoryRequest>()
        val category = categoryRepository.createCategory(request.toCategory())
        call.successRespond(
            data = category.toResponse(call.getBaseUrl()),
            message = SuccessMessage.CATEGORY_CREATED
        )
    }
}

private fun Route.updateCategory(categoryRepository: CategoryRepository) {
    put {
        val request = call.receive<UpdateCategoryRequest>()
        val category = categoryRepository.updateCategory(request.toCategory())
            ?: throw AppException.NotFound()
        call.successRespond(
            data = category.toResponse(call.getBaseUrl()),
            message = SuccessMessage.CATEGORY_UPDATED
        )
    }
}

private fun Route.deleteCategory(categoryRepository: CategoryRepository) {
    delete("/{id}") {
        val id = call.parameters["id"]?.toUUID() ?: throw AppException.BadRequest()
        val deleted = categoryRepository.deleteCategory(id)
        if (deleted) {
            call.successRespond(data = true, message = SuccessMessage.CATEGORY_DELETED)
        } else {
            throw AppException.NotFound()
        }
    }
}