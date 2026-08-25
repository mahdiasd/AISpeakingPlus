package ir.aispeaking.data.mapper.scenario;

import ir.aispeaking.domain.model.scenario.ScenarioDetail
import ir.aispeaking.network.dto.scenario.ScenarioDetailResponse

fun ScenarioDetailResponse.toDomain(): ScenarioDetail {
    return ScenarioDetail(
        scenario = scenario.toDomain(),
        userHaveSubscription = userHaveSubscription,
        progress = progress?.toDomain()
    )
}
