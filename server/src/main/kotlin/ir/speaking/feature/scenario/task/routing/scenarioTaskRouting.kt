package ir.speaking.feature.scenario.task.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.scenario.task.dto.toResponse
import ir.speaking.feature.scenario.task.repository.ScenarioTaskRepository
import org.koin.ktor.ext.inject

fun Application.scenarioTaskRouting() {
    val scenarioRepository by inject<ScenarioTaskRepository>()

    routing {
        route("/api/v1/scenario-tasks") {
            getScenarioTasksByScenarioId(scenarioRepository)
        }
    }
}

private fun Route.getScenarioTasksByScenarioId(scenarioRepository: ScenarioTaskRepository) {
    get("/{scenarioId}") {
        val scenarioId = call.parameters["scenarioId"]?.toUUID() ?: throw AppException.BadRequest()
        val tasks = scenarioRepository.getScenarioTasksByScenarioId(scenarioId)
        call.successRespond(tasks.map { it.toResponse() })
    }
}
