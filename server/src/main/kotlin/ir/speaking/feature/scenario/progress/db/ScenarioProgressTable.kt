package ir.speaking.feature.scenario.progress.db

import ir.speaking.feature.scenario.progress.model.ScenarioProgress
import ir.speaking.feature.scenario.scenario.db.ScenarioTable
import ir.speaking.feature.user.db.UserTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*

object ScenarioProgressTable : UUIDTable("scenario_progress") {
    val userId = uuid("user_id").references(UserTable.id)
    val scenarioId = uuid("scenario_id").references(ScenarioTable.id)
    val score = integer("score")
    val completedAt = datetime("completed_at")
}

class ScenarioProgressDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ScenarioProgressDAO>(ScenarioProgressTable)

    var userId by ScenarioProgressTable.userId
    var scenarioId by ScenarioProgressTable.scenarioId
    var score by ScenarioProgressTable.score
    var completedAt by ScenarioProgressTable.completedAt
}

fun ScenarioProgressDAO.toModel() = ScenarioProgress(
    id = id.value,
    userId = userId,
    scenarioId = scenarioId,
    score = score,
    completedAt = completedAt
)