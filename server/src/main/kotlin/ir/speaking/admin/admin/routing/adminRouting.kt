package ir.speaking.admin.admin.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.admin.admin.dto.CreateAdminRequest
import ir.speaking.admin.admin.dto.LoginAdminRequest
import ir.speaking.admin.admin.repository.AdminRepository
import ir.speaking.core.generateToken
import ir.speaking.core.network.model.AiPlatformConfig
import ir.speaking.core.redis.ai.AiConfigRedisRepository
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.message.SuccessMessage
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import org.koin.ktor.ext.inject

fun Application.adminRouting() {
    val repo by inject<AdminRepository>()
    val aiConfigRedisRepository by inject<AiConfigRedisRepository>()

    routing {
        route("/api/v1/admin") {
            loginAdmin(repo)
            authenticate(MyConstant.ADMIN_JWT_NAME) {
                createAdmin(repo)
                aiConfigRoutes(aiConfigRedisRepository)
            }
        }
    }
}

private fun Route.loginAdmin(repo: AdminRepository) {
    post("/login") {
        val request = call.receive<LoginAdminRequest>()

        val admin = repo.login(request.username, request.password)

        call.successRespond(
            data = generateToken(
                routing = this@loginAdmin,
                uid = admin.uid.toString(),
                adminToken = true
            ),
            message = "Login successful"
        )
    }
}

private fun Route.createAdmin(repo: AdminRepository) {
    post("/create") {
        val request = call.receive<CreateAdminRequest>()

        val user = repo.createAdmin(request.username, request.password)

        call.successRespond(
            data = generateToken(
                routing = this@createAdmin,
                uid = user.uid.toString(),
                adminToken = true
            ),
            message = SuccessMessage.USER_CREATED
        )
    }
}


fun Route.aiConfigRoutes(aiConfigRedisRepository: AiConfigRedisRepository) {
    route("/admin/ai-config") {

        get {
            val config = aiConfigRedisRepository.getConfig()
            if (config != null) {
                call.successRespond( config)
            } else {
                call.failureRespond(
                    HttpStatusCode.NotFound,
                    "No custom configuration found. Using hardcoded fallback."
                )
            }
        }

        post {
            try {
                val newConfig = call.receive<AiPlatformConfig>()
                aiConfigRedisRepository.setConfig(newConfig)
                call.successRespond(
                    "AI Configuration updated successfully. Traffic routed to new platform."
                )
            } catch (e: Exception) {
                call.failureRespond(
                    HttpStatusCode.BadRequest,
                    e.message.toString()
                )
            }
        }
    }
}