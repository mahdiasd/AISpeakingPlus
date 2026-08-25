package ir.speaking.feature.scenario.task.db

import ir.speaking.feature.scenario.scenario.db.ScenarioTable
import ir.speaking.feature.scenario.task.model.ScenarioTask
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*

object ScenarioTaskTable : UUIDTable("scenario_task") {
    val scenarioId = uuid("scenario_id").references(ScenarioTable.id).index()
    val description = text("description")
    val persianDescription = text("persian_description")
    val createdAt = datetime("created_at")
}

class ScenarioTaskDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ScenarioTaskDAO>(ScenarioTaskTable)

    var scenarioId by ScenarioTaskTable.scenarioId
    var description by ScenarioTaskTable.description
    var persianDescription by ScenarioTaskTable.persianDescription
    var createdAt by ScenarioTaskTable.createdAt

}

fun ScenarioTaskDAO.toModel() = ScenarioTask(
    id = id.value,
    scenarioId = scenarioId,
    description = description,
    persianDescription = persianDescription,
    createdAt = createdAt
)