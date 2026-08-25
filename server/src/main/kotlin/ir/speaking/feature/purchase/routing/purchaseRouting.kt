package ir.speaking.feature.purchase.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getUserUid
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.purchase.dto.PurchaseRequest
import ir.speaking.feature.purchase.repository.PurchaseRepository
import org.koin.ktor.ext.inject

fun Application.purchaseRouting() {
    val purchaseRepository by inject<PurchaseRepository>()

    routing {
        route("/api/v1/purchase") {
            createPurchaseRoute(purchaseRepository)
            getPurchasesByUserIdRoute(purchaseRepository)
            deletePurchaseRoute(purchaseRepository)
            getActiveUserPurchases(purchaseRepository)
        }
    }
}

private fun Route.createPurchaseRoute(purchaseRepository: PurchaseRepository) {
    authenticate(MyConstant.USER_JWT_NAME) {
        post {
            val request = call.receive<PurchaseRequest>()
            if (request.id != null) throw AppException.BadRequest()
            val createdPurchase = purchaseRepository.createPurchase(request)
            call.successRespond(createdPurchase)
        }
    }
}

private fun Route.deletePurchaseRoute(purchaseRepository: PurchaseRepository) {
    authenticate(MyConstant.ADMIN_JWT_NAME) {
        delete("/{id}") {
            val id = call.parameters["id"]?.toUUID() ?: throw AppException.BadRequest()
            val deleted = purchaseRepository.deletePurchase(id)
            if (deleted) call.successRespond("Deleted successfully")
            else throw AppException.NotFound()
        }
    }
}


private fun Route.getPurchasesByUserIdRoute(purchaseRepository: PurchaseRepository) {
    authenticate(MyConstant.USER_JWT_NAME) {
        get("/user") {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

            val userId = call.getUserUid()
            val pagedPurchases = purchaseRepository.getPurchasesByUserId(userId = userId, page = page, pageSize = size)
            call.successRespond(
                data = pagedPurchases.items.filter { it.plan.name != "هدیه" },
                pagingMeta = pagedPurchases.pagingMeta
            )
        }
    }
}

private fun Route.getActiveUserPurchases(purchaseRepository: PurchaseRepository) {
    authenticate(MyConstant.USER_JWT_NAME) {
        get("/user/active") {
            val userId = call.getUserUid()
            call.successRespond(purchaseRepository.getActivePurchasesByUserId(userId = userId))
        }
    }
}