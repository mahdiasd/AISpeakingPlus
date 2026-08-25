package ir.speaking.admin.scenario.scenario.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ir.speaking.admin.scenario.scenario.dto.SaveScenarioRequest
import ir.speaking.admin.scenario.scenario.dto.toScenario
import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.*
import ir.speaking.feature.scenario.scenario.dto.response.toResponse
import ir.speaking.feature.scenario.scenario.repository.ScenarioRepository
import kotlinx.serialization.SerializationException
import org.koin.ktor.ext.inject

fun Application.adminScenarioRouting() {
    val scenarioRepository by inject<ScenarioRepository>()

    routing {
        route("/api/v1/admin/scenarios") {
            authenticate(MyConstant.ADMIN_JWT_NAME) {
            createScenario(scenarioRepository)
            updateScenario(scenarioRepository)
            deleteScenario(scenarioRepository)
            }
        }
    }
}

private fun Route.createScenario(scenarioRepository: ScenarioRepository) {
    post {
        try {
            val multipart = call.receiveMultipart()
            val request = parseMultipartData<SaveScenarioRequest>(multipart)

            // Save image if provided
            val imageUrl = request.imageFile?.let { imageData ->
                ImageSaver.saveImage(imageData.fileName, imageData.fileBytes, ImageType.SCENARIO)
            } ?: request.imageUrl

            // Create scenario with the uploaded image URL
            val scenario = scenarioRepository.createScenario(request.copy(imageUrl = imageUrl).toScenario())

            call.successRespond(scenario.toResponse { call.getFullPath(it) })

        } catch (e: Exception) {
            when (e) {
                is AppException -> throw e
                is SerializationException -> throw AppException.BadRequest("Invalid request data: ${e.message}")
                is IllegalArgumentException -> throw AppException.BadRequest("Invalid request parameters: ${e.message}")
                else -> {
                    println("Error creating scenario: ${e.message}")
                    throw AppException.UnknownError("Failed to create scenario")
                }
            }
        }
    }
}

private fun Route.updateScenario(scenarioRepository: ScenarioRepository) {
    put("/{id}") {
        try {
            val id = call.parameters["id"]?.toUUID() ?: throw AppException.BadRequest("Missing id")
            val multipart = call.receiveMultipart()
            val request = parseMultipartData<SaveScenarioRequest>(multipart)

            // Get existing scenario to preserve data if not updating
            val existingScenario = scenarioRepository.getScenarioById(id)
                ?: throw AppException.NotFound("Scenario not found")

            // Handle image upload/update
            val imageUrl = when {
                request.imageFile != null -> {
                    // Delete old image and save new one
                    existingScenario.imageUrl?.let { ImageSaver.deleteImage(it) }
                    ImageSaver.saveImage(request.imageFile!!.fileName, request.imageFile!!.fileBytes, ImageType.SCENARIO)
                }

                request.imageUrl != null -> request.imageUrl // Use provided URL
                else -> existingScenario.imageUrl // Keep existing
            }

            // Update scenario
            val scenario = scenarioRepository.updateScenario(
                request.copy(
                    id = id.toString(),
                    imageUrl = imageUrl
                ).toScenario()
            ) ?: throw AppException.NotFound("Scenario not found")

            call.successRespond(scenario.toResponse { call.getFullPath(it) })

        } catch (e: Exception) {
            when (e) {
                is AppException -> throw e
                is SerializationException -> throw AppException.BadRequest("Invalid request data: ${e.message}")
                is IllegalArgumentException -> throw AppException.BadRequest("Invalid request parameters: ${e.message}")
                else -> {
                    println("Error updating scenario: ${e.message}")
                    throw AppException.UnknownError("Failed to update scenario")
                }
            }
        }
    }
}

private fun Route.deleteScenario(scenarioRepository: ScenarioRepository) {
    delete("/{id}") {
        try {
            val id = call.parameters["id"]?.toUUID() ?: throw AppException.BadRequest("Missing id")

            // Get scenario to delete associated files
            val scenario = scenarioRepository.getScenarioById(id)
            if (scenario == null) {
                throw AppException.NotFound("Scenario not found")
            }

            // Delete associated image files
            scenario.imageUrl?.let { ImageSaver.deleteImage(it) }
            scenario.aiAvatar?.let { ImageSaver.deleteImage(it) }

            // Delete scenario from database
            val deleted = scenarioRepository.deleteScenario(id)
            if (deleted) {
                call.successRespond("deleted successfully.")
            } else {
                throw AppException.NotFound("Scenario not found")
            }

        } catch (e: Exception) {
            when (e) {
                is AppException -> throw e
                else -> {
                    println("Error deleting scenario: ${e.message}")
                    throw AppException.UnknownError("Failed to delete scenario")
                }
            }
        }
    }
}