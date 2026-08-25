package ir.speaking.core

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import ir.speaking.core.response.failureRespond
import ir.speaking.core.utils.MyConstant

fun Application.configureTestSecurity() {
    // Please read the jwt property from the config file if you are using EngineMain
    val jwtSecret = "secret"
    val issuer = "http://0.0.0.0:8080/"
    val jwtAudience = "http://0.0.0.0:8080/hello"
    val jwtRealm = "Access to 'hello'"
    authentication {
        jwt(MyConstant.USER_JWT_NAME) {
            realm = jwtRealm
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
            challenge { a , b ->
                println("******** $a")
                println("******** $b")
                call.failureRespond(HttpStatusCode.Unauthorized)
            }
        }
    }
}

