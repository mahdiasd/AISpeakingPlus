package ir.speaking.feature.admin.auth.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.generateAdminToken
import ir.speaking.core.response.FailureResponse
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.feature.admin.audit.service.AuditLogService
import ir.speaking.feature.admin.auth.AdminAuthService
import ir.speaking.feature.admin.auth.AdminPrincipal
import ir.speaking.feature.admin.model.AdminInfoDto
import ir.speaking.feature.admin.model.AdminLoginRequest
import ir.speaking.feature.admin.model.AdminLoginResponse

@OptIn(ExperimentalKtorApi::class)
fun Route.adminAuthRouting(
    adminAuthService: AdminAuthService,
    auditLogService: AuditLogService
) {
    route("/api/admin/auth") {

        post("/login") {
            val request = try {
                call.receive<AdminLoginRequest>()
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.BadRequest, "فرمت درخواست نامعتبر است")
                return@post
            }

            val admin = adminAuthService.login(request)
            if (admin == null) {
                call.failureRespond(HttpStatusCode.Unauthorized, "نام کاربری یا رمز عبور نامعتبر است")
                return@post
            }

            val token = generateAdminToken(call, admin.id, admin.username, admin.fullName, admin.role)
            val response = AdminLoginResponse(
                token = token,
                admin = AdminInfoDto(
                    id = admin.id.toString(),
                    username = admin.username,
                    fullName = admin.fullName,
                    role = admin.role
                )
            )

            auditLogService.log(
                adminId = admin.id,
                action = "ADMIN_LOGIN",
                targetType = "ADMIN",
                targetId = admin.id.toString(),
                detailsJson = """{"username":"${admin.username}"}"""
            )

            call.successRespond(response, message = "ورود با موفقیت انجام شد")
        }.describe {
            tag("Admin Auth")
            summary = "Admin Login"
            description = "Authenticate administrator with username and password to receive admin JWT"
            requestBody {
                description = "Admin login credentials"
                required = true
                schema = jsonSchema<AdminLoginRequest>()
            }
            responses {
                HttpStatusCode.OK {
                    description = "Login successful"
                    schema = jsonSchema<SuccessResponse<AdminLoginResponse>>()
                }
                HttpStatusCode.Unauthorized {
                    description = "Invalid credentials"
                    schema = jsonSchema<FailureResponse>()
                }
            }
        }

        authenticate(MyConstant.ADMIN_JWT_NAME) {
            get("/me") {
                val principal = call.principal<AdminPrincipal>()
                if (principal == null) {
                    call.failureRespond(HttpStatusCode.Unauthorized, "دسترسی غیرمجاز")
                    return@get
                }

                val profile = adminAuthService.getProfile(principal.id)
                if (profile == null) {
                    call.failureRespond(HttpStatusCode.NotFound, "حساب مدیر یافت نشد")
                    return@get
                }

                call.successRespond(profile, message = "پروفایل مدیر دریافت شد")
            }.describe {
                tag("Admin Auth")
                summary = "Current Admin Profile"
                description = "Get profile information of current authenticated admin"
                responses {
                    HttpStatusCode.OK {
                        description = "Profile retrieved successfully"
                        schema = jsonSchema<SuccessResponse<AdminInfoDto>>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "Unauthorized"
                        schema = jsonSchema<FailureResponse>()
                    }
                }
            }
        }
    }
}
