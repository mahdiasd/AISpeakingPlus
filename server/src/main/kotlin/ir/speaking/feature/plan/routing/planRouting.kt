package ir.speaking.feature.plan.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.AppUtils
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.discount.repository.DiscountCodeRepository
import ir.speaking.feature.discount.routing.generateBazaarDynamicPriceToken
import ir.speaking.feature.plan.dto.PlanRequest
import ir.speaking.feature.plan.dto.toPlan
import ir.speaking.feature.plan.dto.toResponse
import ir.speaking.feature.plan.repository.PlanRepository
import org.koin.ktor.ext.inject

fun Application.planRouting() {
    val planRepository by inject<PlanRepository>()
    val discountRepository by inject<DiscountCodeRepository>()

    routing {
        route("/api/v1/plan") {
            getAllPlansRoute(planRepository, discountRepository)

            createPlanRoute(planRepository)
            updatePlanRoute(planRepository)
            deletePlanRoute(planRepository)
        }
    }
}

private fun Route.createPlanRoute(planRepository: PlanRepository) {
    authenticate(MyConstant.ADMIN_JWT_NAME) {
        post {
            val request = call.receive<PlanRequest>()
            if (request.id != null) throw AppException.BadRequest()
            val plan = request.toPlan()
            val createdPlan = planRepository.createPlan(plan)
            call.successRespond(createdPlan.toResponse())
        }
    }
}

private fun Route.getAllPlansRoute(
    planRepository: PlanRepository,
    discountRepo: DiscountCodeRepository
) {
    get {
        val visibilityParam = call.request.queryParameters["visibility"]
        val visibilityFilter = visibilityParam?.toBooleanStrictOrNull() ?: true
        val discount = discountRepo.getDiscountCodeByCode("CAMPAIGN_60")

        val plans = planRepository.getAllPlans()
            .filter { it.visibility == visibilityFilter }
            .sortedBy { it.dayDuration }

        if (discount != null) {
            call.successRespond(
                plans.map { plan ->
                    val discountedPrice = AppUtils.calculateDiscountPrice(
                        originalPrice = plan.price.toLong(),
                        discountPercent = discount.percentage.toLong()
                    )
                    plan.copy(
                        discountedPrice = discountedPrice.toString()
                    ).toResponse(
                        dynamicPriceToken = generateBazaarDynamicPriceToken(
                            sku = plan.cafeBazaarId,
                            price = discountedPrice,
                        ),
                        discountId = discount.id.toString()
                    )
                }
            )
        } else {
            call.successRespond(plans.map { it.toResponse() })
        }
    }
}

private fun Route.updatePlanRoute(planRepository: PlanRepository) {
    authenticate(MyConstant.ADMIN_JWT_NAME) {
        put {
            val request = call.receive<PlanRequest>()
            if (request.id == null) throw AppException.BadRequest()
            val plan = request.toPlan()
            val updatedPlan = planRepository.updatePlan(plan) ?: throw AppException.NotFound()
            call.successRespond(updatedPlan.toResponse())
        }

        put("/bulk-update") {
            val requests = call.receive<List<PlanRequest>>()

            val plansToUpdate = requests
                .filter { it.id != null }
                .map { it.toPlan() }

            val updatedPlans = planRepository.updateMultiplePlans(plansToUpdate)

            if (updatedPlans.isNotEmpty()) {
                call.successRespond(updatedPlans.map { it.toResponse() })
            } else {
                call.failureRespond(HttpStatusCode.BadRequest, "آپدیت پلن‌ها با خطا مواجه شد یا آیدی‌ها نامعتبر بود")
            }
        }
    }
}



private fun Route.deletePlanRoute(planRepository: PlanRepository) {
    authenticate(MyConstant.ADMIN_JWT_NAME) {
        delete("/{id}") {
            val id = call.parameters["id"]?.toUUID() ?: throw AppException.BadRequest()
            val deleted = planRepository.deletePlan(id)
            if (deleted) call.successRespond("Deleted successfully")
            else throw AppException.NotFound()
        }
    }
}
