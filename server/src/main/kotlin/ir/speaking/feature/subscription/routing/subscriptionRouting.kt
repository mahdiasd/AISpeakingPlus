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
import ir.speaking.feature.subscription.service.DynamicDiscountService
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
    val planId: String? = null,
    val market: String? = null
)

@Serializable
data class ValidatePromoResponseDto(
    val promoCode: String,
    val discountPercent: Int = 0,
    val discountType: String = "PERCENTAGE",
    val discountAmountTomans: Long = 0,
    val originalPriceTomans: Long? = null,
    val finalPriceTomans: Long? = null,
    val dynamicDiscountToken: String? = null,
    val bonusDays: Int = 0,
    val userMessage: String? = null,
    val message: String
)

@Serializable
data class DirectCheckoutRequest(
    val planId: String,
    val promoCode: String? = null
)

@Serializable
data class DirectCheckoutResponse(
    val paymentUrl: String,
    val transactionId: String,
    val amountTomans: Long
)

@Serializable
data class VerifyMarketPurchaseRequest(
    val market: String,
    val planId: String,
    val sku: String,
    val purchaseToken: String,
    val orderId: String? = null,
    val developerPayload: String? = null,
    val promoCode: String? = null
)

@Serializable
data class VerifyMarketPurchaseResponseData(
    val isSubscriber: Boolean,
    val planType: String,
    val expiresAt: String,
    val remainingDays: Int,
    val transactionId: String
)

@OptIn(ExperimentalKtorApi::class)
fun Application.subscriptionRouting() {
    val subscriptionRepo by inject<SubscriptionRepo>()
    val dynamicDiscountService by inject<DynamicDiscountService>()
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

            suspend fun RoutingCall.handleValidatePromo() {
                val request = try {
                    receive<ValidatePromoRequestDto>()
                } catch (_: Exception) {
                    failureRespond(HttpStatusCode.BadRequest, "کد تخفیف وارد شده معتبر نیست")
                    return
                }

                val result = adminPromoService.validateForUserAndPlan(
                    code = request.promoCode,
                    userId = null,
                    planId = request.planId
                )

                if (result.isValid) {
                    val baseDays = when (request.planId?.lowercase()?.trim()) {
                        "plan-1m", "1_month" -> 30
                        "plan-3m", "3_months" -> 90
                        "plan-6m", "6_months" -> 180
                        else -> 30
                    }

                    val isMyket = request.market?.equals("MYKET", ignoreCase = true) == true
                    val isBazaar = request.market?.equals("CAFE_BAZAAR", ignoreCase = true) == true

                    val bonusDays = if (isMyket) {
                        (baseDays * result.discountPercent) / 100
                    } else 0

                    val dynamicDiscountToken = if (isBazaar) {
                        dynamicDiscountService.generateDiscountToken(
                            sku = request.planId ?: "plan-1m",
                            discountedPriceTomans = if (result.finalPriceTomans > 0) result.finalPriceTomans else result.originalPriceTomans
                        )
                    } else null

                    val userMessage = when {
                        isMyket -> "با توجه به ساختار پرداخت، معادل ${result.discountPercent}٪ به مدت زمان اشتراک شما ($bonusDays روز هدیه) افزوده خواهد شد."
                        isBazaar -> "تخفیف ${result.discountPercent} درصدی مستقیماً روی فاکتور اعمال گردید."
                        else -> result.message
                    }

                    val responseFinalPrice = if (isMyket) {
                        if (result.originalPriceTomans > 0) result.originalPriceTomans else null
                    } else {
                        if (result.finalPriceTomans > 0) result.finalPriceTomans else null
                    }

                    successRespond(
                        ValidatePromoResponseDto(
                            promoCode = result.normalizedCode,
                            discountPercent = result.discountPercent,
                            discountType = result.discountType,
                            discountAmountTomans = result.discountAmountTomans,
                            originalPriceTomans = if (result.originalPriceTomans > 0) result.originalPriceTomans else null,
                            finalPriceTomans = responseFinalPrice,
                            dynamicDiscountToken = dynamicDiscountToken,
                            bonusDays = bonusDays,
                            userMessage = userMessage,
                            message = result.message
                        ),
                        message = result.message
                    )
                } else {
                    failureRespond(HttpStatusCode.BadRequest, result.message)
                }
            }

            post("/validate-promo") {
                call.handleValidatePromo()
            }.describe {
                tag("Subscriptions")
                summary = "Validate Promo Code"
                description = "Validate a promo discount code for a market and plan"
                requestBody {
                    description = "Promo code payload with optional market identifier"
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

            post("/promo/validate") {
                call.handleValidatePromo()
            }.describe {
                tag("Subscriptions")
                summary = "Validate Promo Code (Legacy Alias)"
                description = "Legacy alias for validate-promo"
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
                        call.failureRespond(HttpStatusCode.Unauthorized, "برای خرید و استفاده از اشتراک، ابتدا وارد حساب کاربری خود شوید.")
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
                        call.failureRespond(HttpStatusCode.Unauthorized, "برای خرید و استفاده از اشتراک، ابتدا وارد حساب کاربری خود شوید.")
                        return@post
                    }

                    val request = try {
                        call.receive<SubscribeRequestDto>()
                    } catch (_: Exception) {
                        call.failureRespond(HttpStatusCode.BadRequest, "Invalid request payload")
                        return@post
                    }

                    val (planType, durationDays) = when (request.planId.trim()) {
                        "plan-1m", "1_MONTH" -> "1_MONTH" to 30
                        "plan-3m", "3_MONTHS" -> "3_MONTHS" to 90
                        "plan-6m", "6_MONTHS" -> "6_MONTHS" to 180
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

                post("/verify-market-purchase") {
                    val principal = call.principal<JWTPrincipal>()
                    val uidString = principal?.payload?.getClaim("uid")?.asString()
                    val userId = uidString?.let { try { UUID.fromString(it) } catch (_: Exception) { null } }

                    if (userId == null) {
                        call.failureRespond(HttpStatusCode.Unauthorized, "برای خرید و استفاده از اشتراک، ابتدا وارد حساب کاربری خود شوید.")
                        return@post
                    }

                    val request = try {
                        call.receive<VerifyMarketPurchaseRequest>()
                    } catch (_: Exception) {
                        call.failureRespond(HttpStatusCode.BadRequest, "Invalid request payload")
                        return@post
                    }

                    if (request.purchaseToken.isBlank()) {
                        call.failureRespond(HttpStatusCode.BadRequest, "Purchase token is required")
                        return@post
                    }

                    val (planType, baseDays) = when (request.planId.trim()) {
                        "plan-1m", "1_MONTH" -> "1_MONTH" to 30
                        "plan-3m", "3_MONTHS" -> "3_MONTHS" to 90
                        "plan-6m", "6_MONTHS" -> "6_MONTHS" to 180
                        else -> "3_MONTHS" to 90
                    }

                    val normalizedPromo = request.promoCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
                    var bonusDays = 0
                    var promoValidation: AdminPromoService.PromoValidationResult? = null

                    if (normalizedPromo != null) {
                        val validation = adminPromoService.validateForUserAndPlan(
                            code = normalizedPromo,
                            userId = userId,
                            planId = request.planId
                        )
                        if (validation.isValid) {
                            promoValidation = validation
                            if (request.market.equals("MYKET", ignoreCase = true)) {
                                bonusDays = (baseDays * validation.discountPercent) / 100
                            }
                        }
                    }

                    val verifyResult = subscriptionRepo.verifyMarketPurchase(
                        ir.speaking.feature.subscription.repository.VerifyMarketPurchaseParams(
                            userId = userId,
                            market = request.market,
                            planId = request.planId,
                            sku = request.sku,
                            purchaseToken = request.purchaseToken,
                            orderId = request.orderId,
                            developerPayload = request.developerPayload,
                            promoCode = normalizedPromo,
                            bonusDays = bonusDays,
                            amountPaidTomans = when (request.planId) {
                                "plan-1m" -> 199_000L
                                "plan-3m" -> 499_000L
                                "plan-6m" -> 899_000L
                                else -> 499_000L
                            }
                        )
                    )

                    if (verifyResult.isSuccess && verifyResult.subscriptionInfo != null) {
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
                            VerifyMarketPurchaseResponseData(
                                isSubscriber = verifyResult.subscriptionInfo.isSubscriber,
                                planType = verifyResult.subscriptionInfo.planType ?: planType,
                                expiresAt = verifyResult.subscriptionInfo.expiresAt ?: "",
                                remainingDays = verifyResult.subscriptionInfo.remainingDays,
                                transactionId = verifyResult.transactionId ?: ""
                            ),
                            message = "اشتراک با موفقیت فعال و حساب شما ارتقا یافت"
                        )
                    } else {
                        val status = if (verifyResult.errorCode == "INVALID_OR_DUPLICATE_PURCHASE_TOKEN") {
                            HttpStatusCode.Conflict
                        } else {
                            HttpStatusCode.BadRequest
                        }
                        call.failureRespond(status, verifyResult.errorMessage ?: "خطا در تایید خرید مارکت")
                    }
                }.describe {
                    tag("Subscriptions")
                    summary = "Verify Market Purchase"
                    description = "Verify market purchase token (Cafe Bazaar / Myket) server-to-server and activate subscription"
                    requestBody {
                        description = "Market purchase token and details"
                        schema = jsonSchema<VerifyMarketPurchaseRequest>()
                    }
                    responses {
                        HttpStatusCode.OK {
                            description = "Market purchase verified and subscription activated successfully"
                            schema = jsonSchema<SuccessResponse<VerifyMarketPurchaseResponseData>>()
                        }
                        HttpStatusCode.BadRequest {
                            description = "Invalid purchase token or verification failed"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.Conflict {
                            description = "Duplicate or already used purchase token"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.Unauthorized {
                            description = "Authentication required or invalid token"
                            schema = jsonSchema<FailureResponse>()
                        }
                    }
                }

                post("/direct-checkout") {
                    val principal = call.principal<JWTPrincipal>()
                    val uidString = principal?.payload?.getClaim("uid")?.asString()
                    val userId = uidString?.let { try { UUID.fromString(it) } catch (_: Exception) { null } }

                    if (userId == null) {
                        call.failureRespond(HttpStatusCode.Unauthorized, "برای خرید و استفاده از اشتراک، ابتدا وارد حساب کاربری خود شوید.")
                        return@post
                    }

                    val request = try {
                        call.receive<DirectCheckoutRequest>()
                    } catch (_: Exception) {
                        call.failureRespond(HttpStatusCode.BadRequest, "Invalid request payload")
                        return@post
                    }

                    val basePrice = adminPromoService.getPlanPrice(request.planId) ?: 199_000L
                    var finalPrice = basePrice
                    val normalizedPromo = request.promoCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
                    if (normalizedPromo != null) {
                        val validation = adminPromoService.validateForUserAndPlan(
                            code = normalizedPromo,
                            userId = userId,
                            planId = request.planId
                        )
                        if (validation.isValid && validation.finalPriceTomans > 0) {
                            finalPrice = validation.finalPriceTomans
                        }
                    }

                    val txId = UUID.randomUUID().toString()
                    val gatewayBaseUrl = System.getenv("PAYMENT_GATEWAY_URL") ?: "https://gateway.aispeaking.ir/pay"
                    val paymentUrl = "$gatewayBaseUrl/$txId?amount=$finalPrice"

                    call.successRespond(
                        DirectCheckoutResponse(
                            paymentUrl = paymentUrl,
                            transactionId = txId,
                            amountTomans = finalPrice
                        ),
                        message = "شناسه پرداخت با موفقیت ایجاد شد"
                    )
                }.describe {
                    tag("Subscriptions")
                    summary = "Create Direct Web Payment"
                    description = "Generate a direct payment gateway URL for sideload or web clients"
                    requestBody {
                        description = "Direct checkout request payload"
                        required = true
                        schema = jsonSchema<DirectCheckoutRequest>()
                    }
                    responses {
                        HttpStatusCode.OK {
                            description = "Direct payment URL generated successfully"
                            schema = jsonSchema<SuccessResponse<DirectCheckoutResponse>>()
                        }
                        HttpStatusCode.BadRequest {
                            description = "Invalid request payload"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.Unauthorized {
                            description = "Authentication required or invalid token"
                            schema = jsonSchema<FailureResponse>()
                        }
                    }
                }
            }
        }
    }
}

