package ir.speaking.feature.subscription.interceptor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ir.speaking.core.response.failureRespond
import ir.speaking.feature.stage.routing.StageErrorResponse
import ir.speaking.feature.subscription.repository.SubscriptionRepo
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.koin.ktor.ext.inject
import java.util.*

fun Route.requireSubscription(build: Route.() -> Unit): Route {
    val subRoute = createChild(object : RouteSelector() {
        override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
            return RouteSelectorEvaluation.Constant
        }
    })

    val subscriptionRepo by subRoute.inject<SubscriptionRepo>()

    subRoute.intercept(ApplicationCallPipeline.Plugins) {
        val principal = call.principal<JWTPrincipal>()
        val uidString = principal?.payload?.getClaim("uid")?.asString()
        val userId = uidString?.let { try { UUID.fromString(it) } catch (_: Exception) { null } }

        if (userId == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                StageErrorResponse(
                    status = 401,
                    message = "ورود به حساب کاربری الزامی است.",
                    code = "AUTH_REQUIRED"
                )
            )
            finish()
            return@intercept
        }

        val isSuspended = org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction(kotlinx.coroutines.Dispatchers.IO) {
            ir.speaking.feature.user.db.UserTable
                .selectAll()
                .where { (ir.speaking.feature.user.db.UserTable.id eq userId) and (ir.speaking.feature.user.db.UserTable.status eq "SUSPENDED") }
                .count() > 0
        }

        if (isSuspended) {
            call.respond(
                HttpStatusCode.Forbidden,
                StageErrorResponse(
                    status = 403,
                    message = "حساب کاربری شما تعلیق شده است.",
                    code = "ACCOUNT_SUSPENDED"
                )
            )
            finish()
            return@intercept
        }

        val hasActive = subscriptionRepo.hasActiveSubscription(userId)
        if (!hasActive) {
            call.respond(
                HttpStatusCode.PaymentRequired,
                StageErrorResponse(
                    status = 402,
                    message = "برای دسترسی به این مرحله، اشتراک ویژه تهیه کنید.",
                    code = "SUBSCRIPTION_REQUIRED"
                )
            )
            finish()
            return@intercept
        }
    }

    subRoute.build()
    return subRoute
}

suspend fun ApplicationCall.enforceNotSuspended(userId: UUID?): Boolean {
    if (attributes.getOrNull(ir.speaking.core.SuspendedUserAttributeKey) == true) {
        respond(
            HttpStatusCode.Forbidden,
            StageErrorResponse(
                status = 403,
                message = "حساب کاربری شما تعلیق شده است.",
                code = "ACCOUNT_SUSPENDED"
            )
        )
        return false
    }
    if (userId != null) {
        val isSuspended = try {
            org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction(kotlinx.coroutines.Dispatchers.IO) {
                ir.speaking.feature.user.db.UserTable
                    .selectAll()
                    .where { (ir.speaking.feature.user.db.UserTable.id eq userId) and (ir.speaking.feature.user.db.UserTable.status eq "SUSPENDED") }
                    .count() > 0
            }
        } catch (_: Exception) {
            false
        }
        if (isSuspended) {
            respond(
                HttpStatusCode.Forbidden,
                StageErrorResponse(
                    status = 403,
                    message = "حساب کاربری شما تعلیق شده است.",
                    code = "ACCOUNT_SUSPENDED"
                )
            )
            return false
        }
    }
    return true
}

suspend fun ApplicationCall.enforceStageAccess(
    stage: ir.speaking.feature.stage.dto.StageDetailResponse?,
    userId: UUID?
): Boolean {
    if (!enforceNotSuspended(userId)) {
        return false
    }

    if (stage == null) {
        failureRespond(HttpStatusCode.NotFound, "Stage not found")
        return false
    }

    if (stage.lockStatus == "LOCKED_REGISTRATION" || (stage.orderIndex == 2 && userId == null)) {
        respond(
            HttpStatusCode.Unauthorized,
            StageErrorResponse(
                status = 401,
                message = "ثبت‌نام برای ورود به مرحله ۲ الزامی است.",
                code = "AUTH_REQUIRED"
            )
        )
        return false
    }

    if (stage.orderIndex >= 3 || stage.lockStatus == "LOCKED_SUBSCRIPTION") {
        if (userId == null) {
            respond(
                HttpStatusCode.Unauthorized,
                StageErrorResponse(
                    status = 401,
                    message = "ورود به حساب کاربری الزامی است.",
                    code = "AUTH_REQUIRED"
                )
            )
            return false
        }
        if (stage.lockStatus == "LOCKED_SUBSCRIPTION") {
            respond(
                HttpStatusCode.PaymentRequired,
                StageErrorResponse(
                    status = 402,
                    message = "برای دسترسی به مرحله ۳ به بعد، اشتراک ویژه تهیه کنید.",
                    code = "SUBSCRIPTION_REQUIRED"
                )
            )
            return false
        }
    }

    return true
}
