package ir.speaking.feature.challenge.challenge.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.exeptions.ErrorMessage
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getFullPath
import ir.speaking.core.utils.getUserUidOrNull
import ir.speaking.feature.challenge.challenge.dto.toSummaryResponse
import ir.speaking.feature.challenge.challenge.mapper.toResponse
import ir.speaking.feature.challenge.challenge.repository.ChallengeRepository
import ir.speaking.feature.challenge.progress.repository.ChallengeProgressRepository
import ir.speaking.feature.purchase.repository.PurchaseRepository
import ir.speaking.feature.scenario.scenario.dto.response.ScenarioDetailResponse
import ir.speaking.feature.user.repository.UserRepository
import org.koin.ktor.ext.inject


fun Application.challengeRouting() {
    val challengeRepository by inject<ChallengeRepository>()
    val challengeProgressRepository by inject<ChallengeProgressRepository>()
    val purchaseRepository by inject<PurchaseRepository>()
    val userRepository by inject<UserRepository>()

    routing {
        getLastActiveChallengeRoute(challengeRepository)
        getChallengeDetailRoute(
            challengeRepository,
            userRepository,
            purchaseRepository,
            challengeProgressRepository
        )
    }
}

private fun Route.getLastActiveChallengeRoute(
    challengeRepository: ChallengeRepository
) {
    authenticate(MyConstant.USER_JWT_NAME, optional = true) {
        get("/api/v1/daily-challenge") {
            val challenge = challengeRepository.readLastActive()
                ?: challengeRepository.readLast()
                ?: throw AppException.Gone(message = ErrorMessage.GONE_ACTIVE_CHALLENGE)

            call.successRespond(challenge.toSummaryResponse { call.getFullPath(it) })
        }
    }
}

private fun Route.getChallengeDetailRoute(
    challengeRepository: ChallengeRepository,
    userRepository: UserRepository,
    purchaseRepository: PurchaseRepository,
    challengeProgressRepository: ChallengeProgressRepository
) {
    authenticate(MyConstant.USER_JWT_NAME, optional = true) {
        get("/api/v1/daily-challenge-detail") {
            val challengeId = call.request.queryParameters["challengeId"] ?: throw AppException.NotFound()
            val challenge = challengeRepository.get(challengeId)
                ?: throw AppException.Gone(message = ErrorMessage.GONE_ACTIVE_CHALLENGE)

            val userId = call.getUserUidOrNull()

            if (userId != null && userRepository.getUserById(userId) == null) {
                throw AppException.UnauthorizedAccess() // user token expired
            }

            val haveSubscription = if (userId != null) purchaseRepository.getActivePurchasesByUserId(userId).isNotEmpty()
            else false

            call.successRespond(
                ScenarioDetailResponse(
                    scenario = challenge.toResponse { call.getFullPath(it) },
                    userHaveSubscription = haveSubscription,
                    progress = userId?.let {
                        challengeProgressRepository.get(it, challengeId = challenge.uid)?.toResponse()
                    }
                )
            )
        }
    }
}
