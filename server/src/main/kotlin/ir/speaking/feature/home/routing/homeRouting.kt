package ir.speaking.feature.home.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.getBaseUrl
import ir.speaking.core.utils.getFullPath
import ir.speaking.core.utils.toJsonElement
import ir.speaking.feature.category.dto.response.toResponse
import ir.speaking.feature.category.repository.CategoryRepository
import ir.speaking.feature.home.dto.HomeResponse
import ir.speaking.feature.home.dto.HomeType
import ir.speaking.feature.home.model.Banner
import ir.speaking.feature.home.model.LinkType
import ir.speaking.feature.home.repository.HomeRedisRepository
import ir.speaking.feature.scenario.scenario.repository.ScenarioRepository
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject


fun Application.homeRouting() {
    val scenarioRepo by inject<ScenarioRepository>()
    val categoryRepo by inject<CategoryRepository>()
    val homeRedisRepo by inject<HomeRedisRepository>()

    routing {
        route("/api/v1/home") {
            getHome(scenarioRepo, categoryRepo, homeRedisRepo)

            post("/cache-clear") {
                homeRedisRepo.clearCache()
                call.successRespond("Home cache cleared successfully.")
            }
        }
    }
}

private fun Route.getHome(
    scenarioRepo: ScenarioRepository,
    categoryRepo: CategoryRepository,
    homeRedisRepo: HomeRedisRepository
) {
    get {
        // 3. Try to get from cache first (Cache-Aside pattern)
        val cachedResponseJson = homeRedisRepo.readHomeResponse()
        if (cachedResponseJson != null) {
            val cachedResponse = Json.decodeFromString<List<HomeResponse>>(cachedResponseJson)
            call.successRespond(cachedResponse)
            return@get
        }

        val list: MutableList<HomeResponse> = mutableListOf()
        val categories = categoryRepo.getAllCategories()

        list.add(
            HomeResponse(
                data = Banner(
                    bannerLink = call.getFullPath("banner/banner_1.webp") ?: "",
                    clickableLink = "",
                    linkType = LinkType.InternalLink
                ).toJsonElement()!!,
                type = HomeType.Banner.name
            )
        )

        list.add(
            HomeResponse(
                title = "Categories",
                data = categories.map { it.toResponse(call.getBaseUrl()) }.sortedBy { it.createdAt }.toJsonElement()!!,
                type = HomeType.Categories.name
            )
        )

        list.add(
            HomeResponse(
                data = scenarioRepo.getScenarios(
                    page = 1,
                    pageSize = 5,
                    searchText = null,
                    category = categories.find { it.name.contains("Resilience") }?.id
                ).items.map { it.copy(imageUrl = call.getFullPath(it.imageUrl)) }.toJsonElement()!!,
                type = HomeType.Scenarios.name,
                title = "Resilience & Hope"
            )
        )

        list.add(
            HomeResponse(
                data = scenarioRepo.getScenarios(
                    page = 1,
                    pageSize = 5,
                    searchText = null,
                    category = categories.find { it.name.contains("Everyday Conversations") }?.id
                ).items.map { it.copy(imageUrl = call.getFullPath(it.imageUrl)) }.toJsonElement()!!,
                type = HomeType.Scenarios.name,
                title = "Everyday Conversations"
            )
        )

        list.add(
            HomeResponse(
                data = Banner(
                    bannerLink = call.getFullPath("banner/banner_2.webp") ?: "",
                    clickableLink = "",
                    linkType = LinkType.InternalLink
                ).toJsonElement()!!,
                type = HomeType.Banner.name
            )
        )
        list.add(
            HomeResponse(
                data = scenarioRepo.getScenarios(
                    page = 1,
                    pageSize = 5,
                    searchText = null,
                    category = categories.find { it.name.contains("Ielts", true) }?.id
                ).items.map { it.copy(imageUrl = call.getFullPath(it.imageUrl)) }.toJsonElement()!!,
                type = HomeType.Scenarios.name,
                title = "IELTS Exam"
            )
        )

        list.add(
            HomeResponse(
                data = scenarioRepo.getScenarios(
                    page = 1,
                    pageSize = 5,
                    searchText = null,
                    category = categories.find { it.name.contains("Academic Life") }?.id
                ).items.map { it.copy(imageUrl = call.getFullPath(it.imageUrl)) }.toJsonElement()!!,
                type = HomeType.Scenarios.name,
                title = "Academic Life"
            )
        )

        list.add(
            HomeResponse(
                data = Banner(
                    bannerLink = call.getFullPath("banner/banner_3.webp") ?: "",
                    clickableLink = "",
                    linkType = LinkType.InternalLink
                ).toJsonElement()!!,
                type = HomeType.Banner.name
            )
        )

        list.add(
            HomeResponse(
                data = scenarioRepo.getScenarios(
                    page = 1,
                    pageSize = 5,
                    searchText = null,
                    category = categories.find { it.name.contains("Tech & Innovation") }?.id
                ).items.map { it.copy(imageUrl = call.getFullPath(it.imageUrl)) }.toJsonElement()!!,
                type = HomeType.Scenarios.name,
                title = "Tech & Innovation"
            )
        )

        homeRedisRepo.saveHomeResponse(list)

        call.successRespond(list)
    }
}
