package ir.speaking.feature.challenge.progress.db

import ir.speaking.feature.challenge.challenge.db.ChallengeTable
import ir.speaking.feature.challenge.progress.model.ChallengeProgress
import ir.speaking.feature.user.db.UserTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*


object ChallengeProgressTable : UUIDTable("challenge_progress") {
    val userId = uuid("user_id").references(UserTable.id)
    val challengeId = uuid("challenge_id").references(ChallengeTable.id)
    val score = integer("score")
    val completedAt = datetime("completed_at")
}

class ChallengeProgressDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ChallengeProgressDAO>(ChallengeProgressTable)

    var userId by ChallengeProgressTable.userId
    var challengeId by ChallengeProgressTable.challengeId
    var score by ChallengeProgressTable.score
    var completedAt by ChallengeProgressTable.completedAt
}

fun ChallengeProgressDAO.toModel() = ChallengeProgress(
    uid = id.value,
    userId = userId,
    challengeId = challengeId,
    score = score,
    completedAt = completedAt
)