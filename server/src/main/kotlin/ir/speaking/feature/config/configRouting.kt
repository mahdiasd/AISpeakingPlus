package ir.speaking.feature.config

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.core.redis.message.MessageRedisRepository
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getUserUidOrNull
import ir.speaking.feature.user.repository.UserRepository
import ir.speaking.feature.user_device_info.model.UserDeviceInfo
import ir.speaking.feature.user_device_info.repository.UserDeviceInfoRepository
import kotlinx.datetime.Clock
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configRouting() {
    val userRepository by inject<UserRepository>()
    val userDeviceInfoRepository by inject<UserDeviceInfoRepository>()
    val messageRedisRepository by inject<MessageRedisRepository>()
    routing {
        route("/api/v1/config") {
            config(userRepository, userDeviceInfoRepository, messageRedisRepository)
        }
    }
}

private fun Route.config(
    userRepository: UserRepository,
    userDeviceInfoRepository: UserDeviceInfoRepository,
    messageRedisRepository: MessageRedisRepository
) {
    authenticate(MyConstant.USER_JWT_NAME, optional = true) {
        post {
                val userId = call.getUserUidOrNull()
                val configRequest = call.receive<ConfigRequest>()
                val tokenAlive = userId != null && userRepository.getUserById(userId) != null

                if (tokenAlive) {
                    userDeviceInfoRepository.upsert(
                        UserDeviceInfo(
                            id = UUID.randomUUID(),
                            userId = userId,
                            firebaseToken = configRequest.firebaseToken,
                            deviceName = configRequest.deviceName,
                            androidVersion = configRequest.androidVersion,
                            lastLogin = Clock.System.now()
                        )
                    )
                }
    val message = messageRedisRepository.getMessage()

                call.successRespond(
                    Config(
                        tokenAlive = tokenAlive,
                        welcomeMessage = message?.copy(imageUrl = ""),
                        update = Update(
                            forceVersion = 7,
                            lastVersion = 8,
                            suggestVersion = 8,
                            link = "https://cafebazaar.ir/app/ir.aispeaking",
                            message = """
                    نسخه جدید: پایداری و بازگشت
                    
                    بازنویسی سیستم اتصال به سرور جهت پایداری حداکثری
                    
                     رفع برخی مشکلات جزئی و بهبود عملکرد کلی برنامه
                """.trimIndent()
                        )
                    )
                )
        }
    }
}
