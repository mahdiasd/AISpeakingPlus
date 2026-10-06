package ir.speaking.feature.subscription.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.FailureResponse
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
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

@OptIn(ExperimentalKtorApi::class)
fun Application.subscriptionRouting() {
    val subscriptionRepo by inject<SubscriptionRepo>()

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
            }
        }
    }
}
