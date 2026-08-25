package ir.aispeaking.domain.model.scenario

data class ScenarioDetail(
    val scenario: Scenario,
    val userHaveSubscription: Boolean,
    val progress: ScenarioProgress?
)