package ir.aispeaking.domain.model.home

import ir.aispeaking.domain.model.banner.Banner
import ir.aispeaking.domain.model.category.Category
import ir.aispeaking.domain.model.scenario.ScenarioSummary
import kotlinx.collections.immutable.ImmutableList

data class Home(
    val title: String?,
    val data: HomeData
)

sealed class HomeData {
    data class BannerData(val banner: Banner) : HomeData()

    data class CategoriesData(val categories: ImmutableList<Category>) : HomeData()

    data class ScenariosData(val scenarios: ImmutableList<ScenarioSummary>) : HomeData()
}
