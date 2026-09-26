package ir.speaking.feature.user.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.generateToken
import ir.speaking.core.response.FailureResponse
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.AppUtils
import ir.speaking.core.utils.getUserUid
import ir.speaking.feature.user.dto.*
import ir.speaking.feature.user.service.AuthService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.ktor.ext.inject

@OptIn(ExperimentalKtorApi::class)
fun Application.authRouting() {
    val authService by inject<AuthService>()

    routing {
        route("/api/v2/auth") {
            post("/otp/send") {
                val bodyText = try {
                    call.receiveText().trim()
                } catch (_: Exception) {
                    ""
                }

                var mobileNumber: String? = null

                if (bodyText.isNotBlank()) {
                    if (bodyText.startsWith("{") && bodyText.endsWith("}")) {
                        try {
                            val req = Json { isLenient = true; ignoreUnknownKeys = true }.decodeFromString<SendOtpRequest>(bodyText)
                            mobileNumber = req.getNormalizedMobile()
                        } catch (_: Exception) {
                            try {
                                val jsonElem = Json.parseToJsonElement(bodyText).jsonObject
                                val extracted = jsonElem["mobile"]?.jsonPrimitive?.contentOrNull
                                    ?: jsonElem["phoneNumber"]?.jsonPrimitive?.contentOrNull
                                    ?: jsonElem["phone"]?.jsonPrimitive?.contentOrNull
                                    ?: jsonElem["mobileNumber"]?.jsonPrimitive?.contentOrNull
                                    ?: jsonElem["mobile_number"]?.jsonPrimitive?.contentOrNull
                                mobileNumber = extracted?.let { AppUtils.normalizeMobileNumber(it) }
                            } catch (_: Exception) {
                                // Fallthrough
                            }
                        }
                    } else if (bodyText.contains("=") && !bodyText.startsWith("{")) {
                        // Form urlencoded or key=value: mobile=09152413498
                        val params = bodyText.split("&").associate {
                            val parts = it.split("=", limit = 2)
                            parts[0] to parts.getOrElse(1) { "" }
                        }
                        val extracted = params["mobile"] ?: params["phoneNumber"] ?: params["phone"]
                        mobileNumber = extracted?.let { AppUtils.normalizeMobileNumber(it) }
                    } else {
                        // Raw string or number passed directly as body: e.g. "09152413498" or 09152413498
                        val stripped = bodyText.removeSurrounding("\"").trim()
                        mobileNumber = AppUtils.normalizeMobileNumber(stripped)
                    }
                }

                // Fallback to query parameter (e.g. ?mobile=09152413498)
                if (mobileNumber.isNullOrBlank()) {
                    val param = call.request.queryParameters["mobile"]
                        ?: call.request.queryParameters["phoneNumber"]
                        ?: call.request.queryParameters["phone"]
                    if (!param.isNullOrBlank()) {
                        mobileNumber = AppUtils.normalizeMobileNumber(param)
                    }
                }

                if (mobileNumber.isNullOrBlank()) {
                    call.failureRespond(
                        HttpStatusCode.BadRequest,
                        "شماره موبایل الزامی است. نمونه بدنه درخواست: {\"mobile\": \"09152413498\"}"
                    )
                    return@post
                }

                if (!AppUtils.isValidMobileNumber(mobileNumber)) {
                    call.failureRespond(
                        HttpStatusCode.BadRequest,
                        "فرمت شماره موبایل نامعتبر است. فرمت صحیح: 09152413498 (۱۱ رقم با 09)"
                    )
                    return@post
                }

                val response = authService.sendOtp(mobileNumber)
                call.successRespond(response, message = "OTP code sent successfully")
            }.describe {
                tag("Auth")
                summary = "Send OTP"
                description = "Send 5-digit verification code to mobile number for registration or login"
                requestBody {
                    description = "Mobile number payload (e.g. {\"mobile\": \"09152413498\"})"
                    required = true
                    schema = jsonSchema<SendOtpRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "OTP sent successfully"
                        schema = jsonSchema<SuccessResponse<SendOtpResponse>>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "Invalid mobile number format"
                        schema = jsonSchema<FailureResponse>()
                    }
                }
            }

            post("/otp/verify") {
                val bodyText = try {
                    call.receiveText().trim()
                } catch (_: Exception) {
                    ""
                }

                var mobileNumber: String? = null
                var otpCode: String? = null

                if (bodyText.isNotBlank()) {
                    if (bodyText.startsWith("{") && bodyText.endsWith("}")) {
                        try {
                            val req = Json { isLenient = true; ignoreUnknownKeys = true }.decodeFromString<VerifyOtpRequest>(bodyText)
                            mobileNumber = req.getNormalizedMobile()
                            otpCode = req.getNormalizedOtpCode()
                        } catch (_: Exception) {
                            try {
                                val jsonElem = Json.parseToJsonElement(bodyText).jsonObject
                                if (mobileNumber.isNullOrBlank()) {
                                    val extracted = jsonElem["mobile"]?.jsonPrimitive?.contentOrNull
                                        ?: jsonElem["phoneNumber"]?.jsonPrimitive?.contentOrNull
                                        ?: jsonElem["phone"]?.jsonPrimitive?.contentOrNull
                                        ?: jsonElem["mobileNumber"]?.jsonPrimitive?.contentOrNull
                                        ?: jsonElem["mobile_number"]?.jsonPrimitive?.contentOrNull
                                    mobileNumber = extracted?.let { AppUtils.normalizeMobileNumber(it) }
                                }
                                if (otpCode.isNullOrBlank()) {
                                    val extractedCode = jsonElem["otpCode"]?.jsonPrimitive?.contentOrNull
                                        ?: jsonElem["code"]?.jsonPrimitive?.contentOrNull
                                        ?: jsonElem["otp"]?.jsonPrimitive?.contentOrNull
                                    otpCode = extractedCode?.let { AppUtils.normalizeDigits(it.trim()) }
                                }
                            } catch (_: Exception) {
                                // Fallthrough
                            }
                        }
                    } else if (bodyText.contains("=") && !bodyText.startsWith("{")) {
                        val params = bodyText.split("&").associate {
                            val parts = it.split("=", limit = 2)
                            parts[0] to parts.getOrElse(1) { "" }
                        }
                        val extracted = params["mobile"] ?: params["phoneNumber"] ?: params["phone"]
                        mobileNumber = extracted?.let { AppUtils.normalizeMobileNumber(it) }
                        val extractedCode = params["otpCode"] ?: params["code"] ?: params["otp"]
                        otpCode = extractedCode?.let { AppUtils.normalizeDigits(it.trim()) }
                    }
                }

                // Fallback to query parameters
                if (mobileNumber.isNullOrBlank()) {
                    val param = call.request.queryParameters["mobile"]
                        ?: call.request.queryParameters["phoneNumber"]
                        ?: call.request.queryParameters["phone"]
                    if (!param.isNullOrBlank()) {
                        mobileNumber = AppUtils.normalizeMobileNumber(param)
                    }
                }
                if (otpCode.isNullOrBlank()) {
                    val paramCode = call.request.queryParameters["otpCode"]
                        ?: call.request.queryParameters["code"]
                        ?: call.request.queryParameters["otp"]
                    if (!paramCode.isNullOrBlank()) {
                        otpCode = AppUtils.normalizeDigits(paramCode.trim())
                    }
                }

                if (mobileNumber.isNullOrBlank() || otpCode.isNullOrBlank()) {
                    call.failureRespond(
                        HttpStatusCode.BadRequest,
                        "شماره موبایل و کد تایید الزامی هستند. نمونه درخواست: {\"mobile\": \"09152413498\", \"otpCode\": \"87799\"}"
                    )
                    return@post
                }

                if (!AppUtils.isValidMobileNumber(mobileNumber)) {
                    call.failureRespond(
                        HttpStatusCode.BadRequest,
                        "فرمت شماره موبایل نامعتبر است. فرمت صحیح: 09152413498 (۱۱ رقم با 09)"
                    )
                    return@post
                }

                val authResult = authService.verifyOtp(
                    mobile = mobileNumber,
                    otpCode = otpCode,
                    tokenGenerator = { uid -> generateToken(call = call, uid = uid) }
                )

                if (authResult != null) {
                    call.successRespond(authResult, message = "Authentication successful")
                } else {
                    call.failureRespond(HttpStatusCode.Unauthorized, "کد تایید نامعتبر است یا منقضی شده است")
                }
            }.describe {
                tag("Auth")
                summary = "Verify OTP"
                description = "Verify SMS code, create or authenticate user, and issue JWT Bearer token"
                requestBody {
                    description = "Mobile and OTP code payload (e.g. {\"mobile\": \"09152413498\", \"otpCode\": \"87799\"})"
                    required = true
                    schema = jsonSchema<VerifyOtpRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Authentication successful, token issued"
                        schema = jsonSchema<SuccessResponse<AuthResponse>>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "Missing mobile or OTP code"
                        schema = jsonSchema<FailureResponse>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "Invalid or expired OTP code"
                        schema = jsonSchema<FailureResponse>()
                    }
                }
            }

            authenticate(MyConstant.USER_JWT_NAME) {
                get("/me") {
                    val userId = try {
                        call.getUserUid()
                    } catch (e: Exception) {
                        call.failureRespond(HttpStatusCode.Unauthorized, "Invalid user credentials")
                        return@get
                    }

                    val profile = authService.getUserProfile(userId)
                    if (profile != null) {
                        call.successRespond(profile, message = "User profile retrieved")
                    } else {
                        call.failureRespond(HttpStatusCode.NotFound, "User not found")
                    }
                }.describe {
                    tag("Auth")
                    summary = "Get Current User"
                    description = "Get profile information of currently authenticated user via JWT Bearer token"
                    responses {
                        HttpStatusCode.OK {
                            description = "User profile retrieved successfully"
                            schema = jsonSchema<SuccessResponse<UserProfileResponse>>()
                        }
                        HttpStatusCode.Unauthorized {
                            description = "Authentication required or invalid token"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.NotFound {
                            description = "User not found"
                            schema = jsonSchema<FailureResponse>()
                        }
                    }
                }
            }
        }
    }
}
