package ir.speaking.feature.scenario.scenario.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.*
import ir.speaking.feature.purchase.repository.PurchaseRepository
import ir.speaking.feature.scenario.progress.dto.response.toResponse
import ir.speaking.feature.scenario.progress.repository.ScenarioProgressRepository
import ir.speaking.feature.scenario.scenario.dto.response.ScenarioDetailResponse
import ir.speaking.feature.scenario.scenario.dto.response.toResponse
import ir.speaking.feature.scenario.scenario.repository.ScenarioRepository
import ir.speaking.feature.user.repository.UserRepository
import org.koin.ktor.ext.inject

fun Application.scenarioRouting() {
    val scenarioRepository by inject<ScenarioRepository>()
    val purchaseRepository by inject<PurchaseRepository>()
    val scenarioProgressRepository by inject<ScenarioProgressRepository>()
    val userRepository by inject<UserRepository>()

    routing {
        route("/api/v1") {
            authenticate(MyConstant.USER_JWT_NAME, optional = true) {
                getScenarioById(
                    scenarioRepository = scenarioRepository,
                    purchaseRepository = purchaseRepository,
                    scenarioProgressRepository = scenarioProgressRepository,
                    userRepository = userRepository,
                )
            }

            getScenarios(scenarioRepository)
        }
    }
}


private fun Route.getScenarioById(
    scenarioRepository: ScenarioRepository,
    purchaseRepository: PurchaseRepository,
    scenarioProgressRepository: ScenarioProgressRepository,
    userRepository: UserRepository,
) {
    get("/scenario") {
        val scenarioId = call.queryParameters["id"]?.toUUID() ?: throw AppException.BadRequest()
        val userId = call.getUserUidOrNull()

        if (userId != null && userRepository.getUserById(userId) == null) {
            throw AppException.UnauthorizedAccess() // user token expired
        }

        val scenario = scenarioRepository.getScenarioById(scenarioId)
            ?: throw AppException.NotFound()

        val progress = userId?.let {
            scenarioProgressRepository.getProgressByUserAndScenario(
                userId = it,
                scenarioId = scenarioId
            )?.toResponse()
        }

        call.successRespond(
            ScenarioDetailResponse(
                scenario = scenario.toResponse { call.getFullPath(it) },
                userHaveSubscription = !userId?.let { purchaseRepository.getActivePurchasesByUserId(it) }.isNullOrEmpty(),
                progress = progress
            )
        )
    }
}

private fun Route.getScenarios(scenarioRepository: ScenarioRepository) {
    get("scenarios") {
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
        val searchText = call.request.queryParameters["searchText"]
        val categoryId = call.request.queryParameters["categoryId"]

        val pagedScenario = scenarioRepository.getScenarios(
            page = page,
            pageSize = size,
            searchText = searchText,
            category = if (categoryId.isNullOrEmpty()) null else AppUtils.validUUID(categoryId)
        )

        call.successRespond(
            data = pagedScenario.items.map {
                it.copy(
                    imageUrl = call.getFullPath(it.imageUrl),
                )
            },
            pagingMeta = pagedScenario.pagingMeta
        )
    }
}

