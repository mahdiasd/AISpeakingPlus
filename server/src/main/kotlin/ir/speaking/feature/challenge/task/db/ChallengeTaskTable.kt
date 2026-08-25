package ir.speaking.feature.challenge.task.db

import ir.speaking.feature.challenge.challenge.db.ChallengeTable
import ir.speaking.feature.challenge.task.model.ChallengeTask
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*


object ChallengeTaskTable : UUIDTable("challenge_task") {
    val challengeId = uuid("challenge_id").references(ChallengeTable.id).index()
    val description = text("description")
    val persianDescription = text("persian_description")
    val createdAt = datetime("created_at")
}

class ChallengeTaskDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ChallengeTaskDAO>(ChallengeTaskTable)

    var challengeId by ChallengeTaskTable.challengeId
    var description by ChallengeTaskTable.description
    var persianDescription by ChallengeTaskTable.persianDescription
    var createdAt by ChallengeTaskTable.createdAt

}

fun ChallengeTaskDAO.toModel() = ChallengeTask(
    id = id.value,
    challengeId = challengeId,
    description = description,
    persianDescription = persianDescription,
    createdAt = createdAt
)