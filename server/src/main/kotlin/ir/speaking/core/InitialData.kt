package ir.speaking.core

import io.ktor.server.application.*
import ir.speaking.admin.admin.repository.AdminRepository
import ir.speaking.core.data_provider.Provider
import ir.speaking.core.network.utils.PrintHelper
import ir.speaking.feature.scenario.scenario.repository.ScenarioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject

fun Application.configureSeedData() {
    val scenarioRepo by inject<ScenarioRepository>()
    val adminRepo by inject<AdminRepository>()

    val seedScope = CoroutineScope(Dispatchers.IO)

    seedScope.launch {
        PrintHelper.info(
            scenarioRepo.getScenarios(
                page = 0,
                pageSize = 10,
                searchText = null,
                category = null
            ).items.size.toString(), details = "initData"
        )

        if (scenarioRepo.getScenarios(page = 0, pageSize = 10, searchText = "Reach the Peak", category = null).items.isEmpty()) {
            PrintHelper.info("init condition")
            Provider.initData()
        }
    }
}
