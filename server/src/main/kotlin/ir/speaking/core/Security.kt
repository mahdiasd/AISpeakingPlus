package ir.speaking.core

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import ir.speaking.core.response.failureRespond
import ir.speaking.core.utils.MyConstant
import java.util.*
import java.util.concurrent.TimeUnit


import io.ktor.util.AttributeKey
import ir.speaking.feature.admin.auth.AdminPrincipal
import ir.speaking.feature.user.db.UserTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

val SuspendedUserAttributeKey = AttributeKey<Boolean>("SuspendedUser")

fun Application.configureSecurity() {
    
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()

    authentication {
        jwt(MyConstant.ADMIN_JWT_NAME) {
            realm = "admin-$jwtRealm"
            authHeader { call ->
                val authHeader = call.request.parseAuthorizationHeader()
                if (authHeader != null) {
                    authHeader
                } else {
                    call.request.queryParameters["token"]?.let { token ->
                        io.ktor.http.auth.HttpAuthHeader.Single("Bearer", token)
                    }
                }
            }
            verifier(
                JWT
                    .require(Algorithm.HMAC256("admin-$jwtSecret"))
                    .withAudience("admin-$jwtAudience")
                    .withIssuer("admin-$issuer")
                    .build()
            )
            validate { credential ->
                val uid = credential.payload.getClaim("uid").asString()
                val role = credential.payload.getClaim("role")?.asString() ?: "ROLE_ADMIN"
                val username = credential.payload.getClaim("username")?.asString() ?: ""
                val fullName = credential.payload.getClaim("fullName")?.asString() ?: "Admin"
                if (!uid.isNullOrBlank()) {
                    try {
                        AdminPrincipal(
                            id = UUID.fromString(uid),
                            username = username,
                            fullName = fullName,
                            role = role
                        )
                    } catch (_: Exception) {
                        null
                    }
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.failureRespond(HttpStatusCode.Unauthorized)
            }
        }

        jwt(MyConstant.USER_JWT_NAME) {
            realm = jwtRealm
            authHeader { call ->
                val authHeader = call.request.parseAuthorizationHeader()
                if (authHeader != null) {
                    authHeader
                } else {
                    call.request.queryParameters["token"]?.let { token ->
                        io.ktor.http.auth.HttpAuthHeader.Single("Bearer", token)
                    }
                }
            }
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                val uid = credential.payload.getClaim("uid")?.asString()
                if (!uid.isNullOrBlank()) {
                    val userId = try { UUID.fromString(uid) } catch (_: Exception) { null }
                    if (userId != null) {
                        val isSuspended = try {
                            newSuspendedTransaction(Dispatchers.IO) {
                                UserTable.selectAll()
                                    .where { (UserTable.id eq userId) and (UserTable.status eq "SUSPENDED") }
                                    .count() > 0
                            }
                        } catch (_: Exception) {
                            false
                        }
                        if (isSuspended) {
                            this.attributes.put(SuspendedUserAttributeKey, true)
                            null
                        } else {
                            JWTPrincipal(credential.payload)
                        }
                    } else {
                        JWTPrincipal(credential.payload)
                    }
                } else {
                    null
                }
            }

            challenge { _, _ ->
                if (call.attributes.getOrNull(SuspendedUserAttributeKey) == true) {
                    call.failureRespond(HttpStatusCode.Forbidden, "حساب کاربری شما تعلیق شده است.")
                } else {
                    call.failureRespond(HttpStatusCode.Unauthorized)
                }
            }
        }
    }
}

fun generateToken(call: ApplicationCall, uid: String, adminToken: Boolean = false): String {
    val jwtSecret = call.application.environment.config.propertyOrNull("jwt.secret")?.getString() ?: "secret"
    val issuer = call.application.environment.config.propertyOrNull("jwt.issuer")?.getString() ?: "http://0.0.0.0:8080/"
    val jwtAudience =
        call.application.environment.config.propertyOrNull("jwt.audience")?.getString() ?: "http://0.0.0.0:8080/hello"
    val expirationTime = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30))

    val token = JWT.create()
        .withAudience(if (adminToken) "admin-$jwtAudience" else jwtAudience)
        .withIssuer(if (adminToken) "admin-$issuer" else issuer)
        .withClaim("uid", uid)
        .withExpiresAt(expirationTime)
        .sign(Algorithm.HMAC256(if (adminToken) "admin-$jwtSecret" else jwtSecret))

    return token
}

fun generateToken(routing: Route, uid: String, adminToken: Boolean = false): String {
    val jwtSecret = routing.environment.config.propertyOrNull("jwt.secret")?.getString() ?: "secret"
    val issuer = routing.environment.config.propertyOrNull("jwt.issuer")?.getString() ?: "http://0.0.0.0:8080/"
    val jwtAudience =
        routing.environment.config.propertyOrNull("jwt.audience")?.getString() ?: "http://0.0.0.0:8080/hello"
    val expirationTime = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30))

    val token = JWT.create()
        .withAudience(if (adminToken) "admin-$jwtAudience" else jwtAudience)
        .withIssuer(if (adminToken) "admin-$issuer" else issuer)
        .withClaim("uid", uid)
        .withExpiresAt(expirationTime)
        .sign(Algorithm.HMAC256(if (adminToken) "admin-$jwtSecret" else jwtSecret))

    return token
}

fun generateAdminToken(call: ApplicationCall, id: UUID, username: String, fullName: String, role: String): String {
    val jwtSecret = call.application.environment.config.propertyOrNull("jwt.secret")?.getString() ?: "secret"
    val issuer = call.application.environment.config.propertyOrNull("jwt.issuer")?.getString() ?: "http://0.0.0.0:8080/"
    val jwtAudience =
        call.application.environment.config.propertyOrNull("jwt.audience")?.getString() ?: "http://0.0.0.0:8080/hello"
    val expirationTime = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30))

    return JWT.create()
        .withAudience("admin-$jwtAudience")
        .withIssuer("admin-$issuer")
        .withClaim("uid", id.toString())
        .withClaim("username", username)
        .withClaim("fullName", fullName)
        .withClaim("role", role)
        .withExpiresAt(expirationTime)
        .sign(Algorithm.HMAC256("admin-$jwtSecret"))
}


