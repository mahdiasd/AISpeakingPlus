package ir.speaking.feature.subscription.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.FailureResponse
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.feature.admin.promo.service.AdminPromoService
import ir.speaking.feature.subscription.repository.SubscriptionRepo
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.util.*

@Serializable
data class SubscriptionPlanItem(
    val id: String,
    val type: String,
    val titleFa: String,
    val durationDays: Int,
    val priceTomans: Long,
    val discountPercent: Int = 0,
    val badge: String? = null
)

@Serializable
data class SubscriptionStatusResponse(
    val isSubscriber: Boolean,
    val planType: String? = null,
    val expiresAt: String? = null,
    val remainingDays: Int = 0
)

@Serializable
data class SubscribeRequestDto(
    val planId: String,
    val promoCode: String? = null
)

@Serializable
data class SubscribeResponseDto(
    val isSubscriber: Boolean,
    val planType: String,
    val expiresAt: String,
    val remainingDays: Int,
    val message: String
)

@Serializable
data class ValidatePromoRequestDto(
    val promoCode: String,
    val planId: String? = null
)

@Serializable
data class ValidatePromoResponseDto(
    val promoCode: String,
    val discountPercent: Int = 0,
    val discountType: String = "PERCENTAGE",
    val discountAmountTomans: Long = 0,
    val originalPriceTomans: Long? = null,
    val finalPriceTomans: Long? = null,
    val message: String
)

@OptIn(ExperimentalKtorApi::class)
fun Application.subscriptionRouting() {
    val subscriptionRepo by inject<SubscriptionRepo>()
    val adminPromoService = AdminPromoService()

    routing {
        route("/api/v2/subscriptions") {
            get("/plans") {
                val plans = listOf(
                    SubscriptionPlanItem(
                        id = "plan-1m",
                        type = "1_MONTH",
                        titleFa = "اشتراک ۱ ماهه",
                        durationDays = 30,
                        priceTomans = 199_000L,
                        discountPercent = 0,
                        badge = null
                    ),
                    SubscriptionPlanItem(
                        id = "plan-3m",
                        type = "3_MONTHS",
                        titleFa = "اشتراک ۳ ماهه",
                        durationDays = 90,
                        priceTomans = 499_000L,
                        discountPercent = 15,
                        badge = "محبوب‌ترین"
                    ),
                    SubscriptionPlanItem(
                        id = "plan-6m",
                        type = "6_MONTHS",
                        titleFa = "اشتراک ۶ ماهه",
                        durationDays = 180,
                        priceTomans = 899_000L,
                        discountPercent = 25,
                        badge = "بهترین ارزش"
                    )
                )
                call.successRespond(plans, message = "Subscription plans retrieved")
            }.describe {
                tag("Subscriptions")
                summary = "List Plans"
                description = "Get active subscription plans with pricing, duration, and discounts"
                responses {
                    HttpStatusCode.OK {
                        description = "Subscription plans retrieved successfully"
                        schema = jsonSchema<SuccessResponse<List<SubscriptionPlanItem>>>()
                    }
                    HttpStatusCode.InternalServerError {
                        description = "Server error"
                        schema = jsonSchema<FailureResponse>()
                    }
                }
            }

            post("/promo/validate") {
                val request = try {
                    call.receive<ValidatePromoRequestDto>()
                } catch (_: Exception) {
                    call.failureRespond(HttpStatusCode.BadRequest, "کد تخفیف وارد شده معتبر نیست")
                    return@post
                }

                val result = adminPromoService.validateForUserAndPlan(
                    code = request.promoCode,
                    userId = null,
                    planId = request.planId
                )

                if (result.isValid) {
                    call.successRespond(
                        ValidatePromoResponseDto(
                            promoCode = result.normalizedCode,
                            discountPercent = result.discountPercent,
                            discountType = result.discountType,
                            discountAmountTomans = result.discountAmountTomans,
                            originalPriceTomans = if (result.originalPriceTomans > 0) result.originalPriceTomans else null,
                            finalPriceTomans = if (result.originalPriceTomans > 0) result.finalPriceTomans else null,
                            message = result.message
                        ),
                        message = result.message
                    )
                } else {
                    call.failureRespond(HttpStatusCode.BadRequest, result.message)
                }
            }.describe {
                tag("Subscriptions")
                summary = "Validate Promo Code"
                description = "Validate a promo discount code and get its discount percentage"
                requestBody {
                    description = "Promo code payload"
                    required = true
                    schema = jsonSchema<ValidatePromoRequestDto>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Promo code is valid"
                        schema = jsonSchema<SuccessResponse<ValidatePromoResponseDto>>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "Promo code is invalid or expired"
                        schema = jsonSchema<FailureResponse>()
                    }
                }
            }

            authenticate(MyConstant.USER_JWT_NAME) {
                get("/status") {
                    val principal = call.principal<JWTPrincipal>()
                    val uidString = principal?.payload?.getClaim("uid")?.asString()
                    val userId = uidString?.let { try { UUID.fromString(it) } catch (_: Exception) { null } }

                    if (userId == null) {
                        call.failureRespond(HttpStatusCode.Unauthorized, "User authentication required")
                        return@get
                    }

                    val info = subscriptionRepo.getSubscriptionInfo(userId)
                    call.successRespond(
                        SubscriptionStatusResponse(
                            isSubscriber = info.isSubscriber,
                            planType = info.planType,
                            expiresAt = info.expiresAt,
                            remainingDays = info.remainingDays
                        ),
                        message = "Subscription status retrieved"
                    )
                }.describe {
                    tag("Subscriptions")
                    summary = "Get Subscription Status"
                    description = "Check active subscription plan, expiration date, and remaining days"
                    responses {
                        HttpStatusCode.OK {
                            description = "Subscription status retrieved successfully"
                            schema = jsonSchema<SuccessResponse<SubscriptionStatusResponse>>()
                        }
                        HttpStatusCode.Unauthorized {
                            description = "Authentication required or invalid token"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.InternalServerError {
                            description = "Server error"
                            schema = jsonSchema<FailureResponse>()
                        }
                    }
                }

                post("/subscribe") {
                    val principal = call.principal<JWTPrincipal>()
                    val uidString = principal?.payload?.getClaim("uid")?.asString()
                    val userId = uidString?.let { try { UUID.fromString(it) } catch (_: Exception) { null } }

                    if (userId == null) {
                        call.failureRespond(HttpStatusCode.Unauthorized, "User authentication required")
                        return@post
                    }

                    val request = try {
                        call.receive<SubscribeRequestDto>()
                    } catch (_: Exception) {
                        call.failureRespond(HttpStatusCode.BadRequest, "Invalid request payload")
                        return@post
                    }

                    val (planType, durationDays) = when (request.planId) {
                        "plan-1m" -> "1_MONTH" to 30
                        "plan-3m" -> "3_MONTHS" to 90
                        "plan-6m" -> "6_MONTHS" to 180
                        else -> {
                            call.failureRespond(HttpStatusCode.BadRequest, "شناسه پلن انتخابی نامعتبر است")
                            return@post
                        }
                    }

                    val normalizedPromo = request.promoCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
                    var promoValidation: AdminPromoService.PromoValidationResult? = null

                    if (normalizedPromo != null) {
                        val validation = adminPromoService.validateForUserAndPlan(
                            code = normalizedPromo,
                            userId = userId,
                            planId = request.planId
                        )
                        if (!validation.isValid) {
                            call.failureRespond(HttpStatusCode.BadRequest, validation.message)
                            return@post
                        }
                        promoValidation = validation
                    }

                    val info = subscriptionRepo.activateSubscription(
                        userId = userId,
                        planType = planType,
                        durationDays = durationDays,
                        grantSource = if (normalizedPromo == null) "USER_PURCHASE" else "PROMO_PURCHASE",
                        reason = normalizedPromo?.let { "Purchased with promo code: $it" }
                    )

                    if (promoValidation != null && promoValidation.promoCodeId != null) {
                        adminPromoService.recordRedemption(
                            promoCodeId = promoValidation.promoCodeId,
                            userId = userId,
                            subscriptionId = null,
                            planId = request.planId,
                            originalPrice = promoValidation.originalPriceTomans,
                            discountAmount = promoValidation.discountAmountTomans,
                            finalPrice = promoValidation.finalPriceTomans
                        )
                    }

                    call.successRespond(
                        SubscribeResponseDto(
                            isSubscriber = info.isSubscriber,
                            planType = info.planType ?: planType,
                            expiresAt = info.expiresAt ?: "",
                            remainingDays = info.remainingDays,
                            message = "اشتراک شما با موفقیت فعال گردید"
                        ),
                        message = "Subscription activated successfully"
                    )
                }.describe {
                    tag("Subscriptions")
                    summary = "Subscribe or Extend Plan"
                    description = "Purchase or activate a subscription plan for authenticated user"
                    requestBody {
                        description = "Plan selection and optional promo code"
                        schema = jsonSchema<SubscribeRequestDto>()
                    }
                    responses {
                        HttpStatusCode.OK {
                            description = "Subscription activated successfully"
                            schema = jsonSchema<SuccessResponse<SubscribeResponseDto>>()
                        }
                        HttpStatusCode.BadRequest {
                            description = "Invalid plan ID or payload"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.Unauthorized {
                            description = "Authentication required or invalid token"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.InternalServerError {
                            description = "Server error"
                            schema = jsonSchema<FailureResponse>()
                        }
                    }
                }
            }
        }
    }
}

