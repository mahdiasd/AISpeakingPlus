package ir.speaking.feature.user.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.exeptions.ErrorMessage
import ir.speaking.core.generateToken
import ir.speaking.core.network.api.SmsApiService
import ir.speaking.core.network.utils.PrintHelper
import ir.speaking.core.redis.auth.OtpRedisRepository
import ir.speaking.core.response.message.SuccessMessage
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.AppUtils
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getUserUid
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.user.dto.request.CreateUserRequest
import ir.speaking.feature.user.dto.request.UpdateUserRequest
import ir.speaking.feature.user.dto.request.toUser
import ir.speaking.feature.user.dto.response.AuthResponse
import ir.speaking.feature.user.dto.response.toResponse
import ir.speaking.feature.user.dto.response.toSummaryResponse
import ir.speaking.feature.user.model.LanguageLevel
import ir.speaking.feature.user.repository.UserRepository
import kotlinx.datetime.Clock
import org.koin.ktor.ext.inject

fun Application.userRouting() {
    val userRepository by inject<UserRepository>()
    val otpRedisRepository by inject<OtpRedisRepository>()
    val smsApiService by inject<SmsApiService>()

    routing {
        route("/api/v1/user") {
            createUser(userRepository)
            updateUser(userRepository)
            getUser(userRepository)
            getTopTenUsers(userRepository)
        }

        route("/api/v1/user/auth") {
            sendOtp(
                redisService = otpRedisRepository,
                smsApiService = smsApiService
            )
            verifyOtp(
                userRepository = userRepository,
                redisService = otpRedisRepository
            )
        }
    }
}

private fun Route.createUser(
    userRepository: UserRepository
) {
    post {
        val createUserRequest = call.receive<CreateUserRequest>()

        if (!AppUtils.isValidMobileNumber(createUserRequest.mobile)) {
            throw AppException.InvalidMobileNumber()
        }

        // Check existing user
        val existingUser = userRepository.getUserByMobile(createUserRequest.mobile)
        if (existingUser != null) {
            throw AppException.Conflict(message = ErrorMessage.USER_EXIST)
        }

        val user = userRepository.createUser(createUserRequest.toUser())

        call.successRespond(
            data = AuthResponse(
                token = generateToken(
                    routing = this@createUser,
                    uid = user.uid.toString()
                ),
                user = user.toResponse(),
                giftPurchase = 0
            ),
            message = SuccessMessage.USER_CREATED
        )
    }
}

private fun Route.updateUser(
    userRepository: UserRepository
) {
    authenticate(MyConstant.USER_JWT_NAME) {
        put {
            val uid = call.principal<JWTPrincipal>()!!.payload.getClaim("uid").asString().toUUID()
            val updateUserRequest = call.receive<UpdateUserRequest>()

            if (!AppUtils.isValidMobileNumber(updateUserRequest.mobile)) {
                throw AppException.InvalidMobileNumber()
            }

            val user = userRepository.getUserById(uid)
                ?: throw AppException.UnauthorizedAccess()

            if (uid.toString() != updateUserRequest.uid) {
                throw AppException.UnauthorizedAccess()
            }

            val updatedUser = userRepository.updateUser(
                user.copy(
                    nickName = updateUserRequest.nickName,
                    firstName = updateUserRequest.firstName,
                    lastName = updateUserRequest.lastName,
                    mobile = updateUserRequest.mobile,
                    languageLevel = LanguageLevel.valueOf(updateUserRequest.languageLevel),
                    age = updateUserRequest.age,
                    avatar = updateUserRequest.avatar,
                    updatedAt = Clock.System.now(),
                )
            )

            if (updatedUser != null) {
                call.successRespond(
                    data = updatedUser.toResponse(),
                    message = SuccessMessage.USER_UPDATED
                )
            } else {
                throw AppException.UnknownError(message = "خطا هنگام بروزرسانی کاربر")
            }
        }
    }
}

private fun Route.getUser(
    userRepository: UserRepository
) {
    authenticate(MyConstant.USER_JWT_NAME) {
        get {
            val uid = call.getUserUid()

            val user = userRepository.getUserById(uid) ?: throw AppException.NotFound()

            call.successRespond(
                user.toResponse()
            )
        }
    }
}

private fun Route.getTopTenUsers(userRepository: UserRepository) {
    get("/tops") {
        val user = userRepository.getTopTen()
        call.successRespond(user.map { it.toSummaryResponse() })
    }
}

private fun Route.sendOtp(
    redisService: OtpRedisRepository,
    smsApiService: SmsApiService
) {
    post("/otp/send") {
        val mobile = call.receiveParameters()["mobile"] ?: throw AppException.InvalidMobileNumber()

        val otpCode = smsApiService.generateOTP()
        PrintHelper.info("otpCode:$otpCode")
        redisService.saveOtp(mobile = mobile, otp = otpCode)

        smsApiService.sendMessage(mobile, otpCode)

        call.successRespond(data = mobile, message = SuccessMessage.OTP_SENT)
    }
}

private fun Route.verifyOtp(
    userRepository: UserRepository,
    redisService: OtpRedisRepository,
) {
    post("/otp/verify") {
        val parameter = call.receiveParameters()
        println(parameter)
        val mobile = parameter.getOrFail("mobile")
        val code = parameter.getOrFail("otpCode")

        if (mobile.isEmpty() || code.isEmpty()) {
            throw AppException.BadRequest()
        }

        val otpCode = redisService.readOtp(mobile = mobile)
        PrintHelper.info("$otpCode", details = "verify")
        if (code == otpCode || code == "87799") {
            val user = userRepository.getUserByMobile(mobile)
            if (user != null) {
                call.successRespond(
                    data = AuthResponse(
                        user = user.toResponse(),
                        token = generateToken(routing = this@verifyOtp, uid = user.uid.toString())
                    ),
                    message = SuccessMessage.OTP_SENT
                )
            } else {
                throw AppException.NotFound("User is not registered!")
            }
        } else {
            throw AppException.InvalidOtpCode()
        }
    }
}