package ir.speaking.feature.subscription.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.FailureResponse
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.admin.promo.service.AdminPromoService
import ir.speaking.feature.subscription.db.MarketPaymentTable
import ir.speaking.feature.subscription.repository.SubscriptionRepo
import ir.speaking.feature.subscription.service.DynamicDiscountService
import ir.speaking.feature.subscription.service.ZarinPalService
import ir.speaking.feature.user.db.UserTable
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
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
    val adminPromoService by inject<AdminPromoService>()
    val zarinPalService by inject<ZarinPalService>()

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

            get("/payment/callback") {
                val authority = call.request.queryParameters["Authority"]
                val status = call.request.queryParameters["Status"]
                val webappBaseUrl = System.getenv("WEBAPP_BASE_URL") ?: "https://staging.aispeaking.ir/app"

                if (authority.isNullOrBlank()) {
                    call.respondRedirect("$webappBaseUrl/#subscription?payment_status=failed&error=missing_authority")
                    return@get
                }

                if (status != "OK") {
                    suspendTransaction {
                        MarketPaymentTable.update({ MarketPaymentTable.purchaseToken eq authority }) {
                            it[MarketPaymentTable.status] = "CANCELLED"
                            it[updatedAt] = Clock.System.now()
                        }
                    }
                    call.respondRedirect("$webappBaseUrl/#subscription?payment_status=cancelled&authority=$authority")
                    return@get
                }

                val tx = suspendTransaction {
                    MarketPaymentTable.selectAll()
                        .where { MarketPaymentTable.purchaseToken eq authority }
                        .firstOrNull()
                }

                if (tx == null) {
                    call.respondRedirect("$webappBaseUrl/#subscription?payment_status=failed&error=tx_not_found")
                    return@get
                }

                val currentStatus = tx[MarketPaymentTable.status]
                val txUserId = tx[MarketPaymentTable.userId].value
                val amountTomans = tx[MarketPaymentTable.amountPaidTomans]
                val sku = tx[MarketPaymentTable.sku]
                val bonusDays = tx[MarketPaymentTable.bonusDays]
                val promoCode = tx[MarketPaymentTable.promoCode]

                if (currentStatus == "VERIFIED") {
                    val refId = tx[MarketPaymentTable.orderId] ?: ""
                    call.respondRedirect("$webappBaseUrl/#subscription?payment_status=success&ref_id=$refId")
                    return@get
                }

                val verifyResult = zarinPalService.verifyPayment(
                    authority = authority,
                    amountTomans = amountTomans
                )

                if (verifyResult.isSuccess) {
                    val refId = verifyResult.refId?.toString() ?: "REF_${Clock.System.now().toEpochMilliseconds()}"
                    suspendTransaction {
                        MarketPaymentTable.update({ MarketPaymentTable.purchaseToken eq authority }) {
                            it[MarketPaymentTable.status] = "VERIFIED"
                            it[orderId] = refId
                            it[rawResponse] = verifyResult.rawResponse
                            it[updatedAt] = Clock.System.now()
                        }
                    }

                    val (planType, baseDays) = when (sku.trim()) {
                        "plan-1m", "1_MONTH" -> "1_MONTH" to 30
                        "plan-3m", "3_MONTHS" -> "3_MONTHS" to 90
                        "plan-6m", "6_MONTHS" -> "6_MONTHS" to 180
                        else -> "3_MONTHS" to 90
                    }

                    subscriptionRepo.activateSubscription(
                        userId = txUserId,
                        planType = planType,
                        durationDays = baseDays + bonusDays,
                        grantSource = "DIRECT_ZARINPAL",
                        reason = "Direct ZarinPal purchase (ref: $refId)"
                    )

                    if (!promoCode.isNullOrBlank()) {
                        val promoValidation = adminPromoService.validateForUserAndPlan(
                            code = promoCode,
                            userId = txUserId,
                            planId = sku
                        )
                        if (promoValidation.promoCodeId != null) {
                            adminPromoService.recordRedemption(
                                promoCodeId = promoValidation.promoCodeId,
                                userId = txUserId,
                                subscriptionId = null,
                                planId = sku,
                                originalPrice = promoValidation.originalPriceTomans,
                                discountAmount = promoValidation.discountAmountTomans,
                                finalPrice = amountTomans
                            )
                        }
                    }

                    call.respondRedirect("$webappBaseUrl/#subscription?payment_status=success&ref_id=$refId")
                } else {
                    suspendTransaction {
                        MarketPaymentTable.update({ MarketPaymentTable.purchaseToken eq authority }) {
                            it[MarketPaymentTable.status] = "FAILED"
                            it[rawResponse] = verifyResult.rawResponse ?: verifyResult.errorMessage
                            it[updatedAt] = Clock.System.now()
                        }
                    }
                    val err = verifyResult.errorMessage ?: "verification_failed"
                    call.respondRedirect("$webappBaseUrl/#subscription?payment_status=failed&error=$err")
                }
            }.describe {
                tag("Subscriptions")
                summary = "ZarinPal Payment Callback"
                description = "Handle bank callback from ZarinPal payment gateway and activate subscription"
                responses {
                    HttpStatusCode.Found {
                        description = "Redirect to web app with payment status"
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

                    // SECURITY: Direct activation via /subscribe is strictly restricted to 100% discount promo codes (finalPrice == 0L).
                    // Paid subscriptions MUST go through /direct-checkout (ZarinPal) or /verify-market-purchase (Cafe Bazaar / Myket).
                    val basePrice = adminPromoService.getPlanPrice(request.planId) ?: 199_000L
                    val finalPrice = promoValidation?.finalPriceTomans ?: basePrice

                    if (finalPrice > 0L) {
                        call.failureRespond(
                            HttpStatusCode.PaymentRequired,
                            "فعال‌سازی این اشتراک نیازمند پرداخت است. لطفاً از طریق درگاه پرداخت مستقیم (/direct-checkout) یا خرید مارکت اقدام نمایید."
                        )
                        return@post
                    }

                    val bonusDays = 0
                    val totalDays = durationDays + bonusDays

                    val info = subscriptionRepo.activateSubscription(
                        userId = userId,
                        planType = planType,
                        durationDays = totalDays,
                        grantSource = "PROMO_PURCHASE",
                        reason = normalizedPromo?.let { "100% Free Promo: $it" } ?: "100% Free Promo"
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
                        call.failureRespond(HttpStatusCode.Unauthorized, "User authentication required")
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
                        call.failureRespond(HttpStatusCode.Unauthorized, "User authentication required")
                        return@post
                    }

                    val request = try {
                        call.receive<DirectCheckoutRequest>()
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

                    val basePrice = adminPromoService.getPlanPrice(request.planId) ?: 199_000L
                    var finalPrice = basePrice
                    var bonusDays = 0
                    var promoValidation: AdminPromoService.PromoValidationResult? = null
                    val normalizedPromo = request.promoCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
                    if (normalizedPromo != null) {
                        val validation = adminPromoService.validateForUserAndPlan(
                            code = normalizedPromo,
                            userId = userId,
                            planId = request.planId
                        )
                        if (validation.isValid) {
                            finalPrice = validation.finalPriceTomans
                            bonusDays = 0
                            promoValidation = validation
                        } else {
                            call.failureRespond(HttpStatusCode.BadRequest, validation.message)
                            return@post
                        }
                    }

                    // If 100% discount promo, activate immediately without gateway
                    if (finalPrice == 0L) {
                        val info = subscriptionRepo.activateSubscription(
                            userId = userId,
                            planType = planType,
                            durationDays = durationDays + bonusDays,
                            grantSource = "PROMO_PURCHASE",
                            reason = "Free promo checkout: $normalizedPromo"
                        )
                        if (promoValidation?.promoCodeId != null) {
                            adminPromoService.recordRedemption(
                                promoCodeId = promoValidation.promoCodeId,
                                userId = userId,
                                subscriptionId = null,
                                planId = request.planId,
                                originalPrice = promoValidation.originalPriceTomans,
                                discountAmount = promoValidation.discountAmountTomans,
                                finalPrice = 0L
                            )
                        }
                        val webappBaseUrl = System.getenv("WEBAPP_BASE_URL") ?: "https://staging.aispeaking.ir/app"
                        call.successRespond(
                            DirectCheckoutResponse(
                                paymentUrl = "$webappBaseUrl/#subscription?payment_status=success",
                                transactionId = "FREE_PROMO_${UUID.randomUUID().toString().take(8)}",
                                amountTomans = 0L
                            ),
                            message = "اشتراک رایگان با موفقیت فعال شد"
                        )
                        return@post
                    }

                    val userMobile = suspendTransaction {
                        UserTable.selectAll()
                            .where { UserTable.id eq userId }
                            .firstOrNull()?.get(UserTable.mobile)
                    }

                    val serverPublicUrl = System.getenv("SERVER_PUBLIC_URL") ?: "https://staging.aispeaking.ir"
                    val callbackUrl = "$serverPublicUrl/api/v2/subscriptions/payment/callback"
                    val planTitle = when (planType) {
                        "1_MONTH" -> "اشتراک ۱ ماهه"
                        "3_MONTHS" -> "اشتراک ۳ ماهه"
                        "6_MONTHS" -> "اشتراک ۶ ماهه"
                        else -> "اشتراک"
                    }

                    val paymentResult = zarinPalService.requestPayment(
                        amountTomans = finalPrice,
                        description = "خرید $planTitle هوش مصنوعی AI Speaking",
                        callbackUrl = callbackUrl,
                        mobile = userMobile,
                        orderId = "SUB-${UUID.randomUUID().toString().take(8)}"
                    )

                    if (!paymentResult.isSuccess || paymentResult.authority.isNullOrBlank() || paymentResult.paymentUrl.isNullOrBlank()) {
                        call.failureRespond(
                            HttpStatusCode.BadGateway,
                            paymentResult.errorMessage ?: "خطا در اتصال به درگاه پرداخت زرین‌پال"
                        )
                        return@post
                    }

                    val authority = paymentResult.authority
                    val paymentUrl = paymentResult.paymentUrl

                    val txId = suspendTransaction {
                        MarketPaymentTable.insertAndGetId {
                            it[MarketPaymentTable.userId] = userId
                            it[MarketPaymentTable.market] = "ZARINPAL"
                            it[MarketPaymentTable.sku] = request.planId
                            it[MarketPaymentTable.purchaseToken] = authority
                            it[MarketPaymentTable.amountPaidTomans] = finalPrice
                            it[MarketPaymentTable.promoCode] = normalizedPromo
                            it[MarketPaymentTable.bonusDays] = bonusDays
                            it[MarketPaymentTable.status] = "PENDING"
                        }
                    }

                    call.successRespond(
                        DirectCheckoutResponse(
                            paymentUrl = paymentUrl,
                            transactionId = txId.value.toString(),
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

