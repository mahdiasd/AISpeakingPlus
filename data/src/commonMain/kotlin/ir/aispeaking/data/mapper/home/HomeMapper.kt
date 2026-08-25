package ir.aispeaking.data.mapper.home

import ir.aispeaking.data.mapper.banner.toDomain
import ir.aispeaking.data.mapper.category.toDomain
import ir.aispeaking.data.mapper.scenario.toDomain
import ir.aispeaking.domain.model.home.Home
import ir.aispeaking.domain.model.home.HomeData
import ir.aispeaking.domain.model.home.HomeType
import ir.aispeaking.network.dto.banner.BannerResponse
import ir.aispeaking.network.dto.category.CategoryResponse
import ir.aispeaking.network.dto.home.HomeResponse
import ir.aispeaking.network.dto.scenario.ScenarioSummaryResponse
import ir.aispeaking.utils.fromJson
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonElement

fun HomeResponse.toDomain(): Home {
    return when (this.type) {
        HomeType.Banner.name -> Home(
            title = title,
            data = HomeData.BannerData(this.data.toBanner().toDomain())
        )

        HomeType.Categories.name -> Home(
            title = title,
            data = HomeData.CategoriesData(
                this.data.toCategories().map { it.toDomain() }.toImmutableList()
            )
        )

        HomeType.Scenarios.name -> Home(
            title = title,
            data = HomeData.ScenariosData(
                this.data.toScenarioSummary().map { it.toDomain() }.toImmutableList()
            )
        )

        else -> throw Exception("Home mapper is null")
    }
}

fun JsonElement.toBanner(): BannerResponse {
    return this.toString().fromJson<BannerResponse>() ?: throw Exception("Banner mapper is null")
}

fun JsonElement.toCategories(): List<CategoryResponse> {
    return this.toString().fromJson<List<CategoryResponse>>()
        ?: throw Exception("Banner mapper is null")
}

fun JsonElement.toScenarioSummary(): List<ScenarioSummaryResponse> {
    return this.toString().fromJson<List<ScenarioSummaryResponse>>()
        ?: throw Exception("toScenarioSummary mapper is null")
}