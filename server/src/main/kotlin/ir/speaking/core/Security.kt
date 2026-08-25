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


fun Application.configureSecurity() {
    
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()

    authentication {
        jwt(MyConstant.ADMIN_JWT_NAME) {
            realm = "admin-$jwtRealm"
            verifier(
                JWT
                    .require(Algorithm.HMAC256("admin-$jwtSecret"))
                    .withAudience("admin-$jwtAudience")
                    .withIssuer("admin-$issuer")
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("uid").asString() != "") {
                    JWTPrincipal(credential.payload) // این خط اضافه شود
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
                if (credential.payload.getClaim("uid").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.failureRespond(HttpStatusCode.Unauthorized)
            }
        }
    }
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

