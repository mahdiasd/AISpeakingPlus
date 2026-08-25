package ir.speaking.feature.discount.routing

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.AppUtils
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.discount.dto.request.DiscountCodeRequest
import ir.speaking.feature.discount.dto.request.toDiscountCode
import ir.speaking.feature.discount.dto.response.toResponse
import ir.speaking.feature.discount.repository.DiscountCodeRepository
import ir.speaking.feature.plan.dto.AppliedDiscount
import ir.speaking.feature.plan.dto.PlanResponse
import ir.speaking.feature.plan.dto.toResponse
import ir.speaking.feature.plan.repository.PlanRepository
import org.koin.ktor.ext.inject
import java.time.Instant
import java.util.*

fun Application.discountCodeRouting() {
    val discountCodeRepository by inject<DiscountCodeRepository>()
    val planRepository by inject<PlanRepository>()

    routing {
        route("/api/v1/discount-code") {
            createDiscountCodeRoute(discountCodeRepository)
            getAllDiscountCodesRoute(discountCodeRepository)
            updateDiscountCodeRoute(discountCodeRepository)
            deleteDiscountCodeRoute(discountCodeRepository)
            getDiscountCodeByCodeRoute(discountCodeRepository, planRepository)
        }
    }
}

private fun Route.createDiscountCodeRoute(repo: DiscountCodeRepository) {
    authenticate(MyConstant.ADMIN_JWT_NAME) {
        post {
            val request = call.receive<DiscountCodeRequest>()
            val created = repo.createDiscountCode(request.toDiscountCode())
            call.successRespond(created.toResponse())
        }
    }
}

private fun Route.getAllDiscountCodesRoute(repo: DiscountCodeRepository) {
    authenticate(MyConstant.ADMIN_JWT_NAME) {
        get {
            val codes = repo.getAllDiscountCodes()
            call.successRespond(codes.map { it.toResponse() })
        }
    }
}

private fun Route.updateDiscountCodeRoute(repo: DiscountCodeRepository) {
    authenticate(MyConstant.ADMIN_JWT_NAME) {
        put {
            val request = call.receive<DiscountCodeRequest>()
            if (request.id == null) throw AppException.BadRequest()
            val updated = repo.updateDiscountCode(request.toDiscountCode()) ?: throw AppException.NotFound()
            call.successRespond(updated.toResponse())
        }
    }
}

private fun Route.deleteDiscountCodeRoute(repo: DiscountCodeRepository) {
    authenticate(MyConstant.ADMIN_JWT_NAME) {
        delete("/{id}") {
            val id = call.parameters["id"]?.toUUID() ?: throw AppException.BadRequest()
            val deleted = repo.deleteDiscountCode(id)
            if (deleted) call.successRespond("Deleted successfully")
            else throw AppException.NotFound()
        }
    }
}

private fun Route.getDiscountCodeByCodeRoute(
    repo: DiscountCodeRepository,
    planRepository: PlanRepository
) {
    get("/code/{code}") {
        val code = call.parameters["code"] ?: throw AppException.BadRequest()
        val discount = repo.getDiscountCodeByCode(code) ?: throw AppException.NotFound(message = "کد تخفیف اشتباه است!")
        val plans = planRepository.getAllPlans().filter { it.visibility }

        val plansId = discount.applicablePlans.ifEmpty { plans.map { it.id } }

        val temp = plans.map { plan ->
            if (plan.id in plansId) {
                plan.toResponse().applyDiscount(discount.percentage, discount.id)
            } else {
                plan.toResponse()
            }
        }

        call.successRespond(temp)
    }
}

fun PlanResponse.applyDiscount(percentage: Int, discountId: UUID): PlanResponse {
    val discountedPrice = AppUtils.calculateDiscountPrice(
        originalPrice = this.price.toLong(),
        discountPercent = percentage.toLong()
    )
    val bazaarDiscountToken = generateBazaarDynamicPriceToken(
        sku = this.cafeBazaarId,
        price = AppUtils.calculateDiscountPrice(
            originalPrice = this.price.toLong(),
            discountPercent = percentage.toLong()
        )
    )
    return this.copy(
        discountedPrice = (discountedPrice).toString(),
        appliedDiscount = AppliedDiscount(bazaarToken = bazaarDiscountToken, id = discountId.toString())
    )
}

fun generateBazaarDynamicPriceToken(
    jwtSecret: String = "0lhha_wmtOrIGDXSzGcMFsuBYciuMo7xBIEcObrdtE4",
    packageName: String = "ir.aispeaking",
    sku: String,
    price: Long,
    expirationInSeconds: Long = 60 * 60,  // زمان انقضای توکن به ثانیه
): String {
    val expirationTime = Instant.now().plusSeconds(expirationInSeconds)

    val builder = JWT.create()
        .withClaim("price", price * 10)
        .withClaim("package_name", packageName)
        .withClaim("sku", sku)
        .withExpiresAt(expirationTime)

    return builder.sign(Algorithm.HMAC256(jwtSecret))
}
