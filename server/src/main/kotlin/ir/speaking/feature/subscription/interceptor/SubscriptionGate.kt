package ir.speaking.feature.subscription.interceptor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
