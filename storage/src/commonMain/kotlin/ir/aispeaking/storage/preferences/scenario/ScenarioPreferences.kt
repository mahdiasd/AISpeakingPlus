package ir.aispeaking.storage.preferences.scenario

import ir.aispeaking.storage.model.scenario.SharedScenario

interface ScenarioPreferences {

    fun save(user: SharedScenario)

    fun read(): SharedScenario?
}
