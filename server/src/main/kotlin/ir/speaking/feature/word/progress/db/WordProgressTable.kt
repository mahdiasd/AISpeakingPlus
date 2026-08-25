package ir.speaking.feature.word.progress.db

import ir.speaking.feature.user.db.UserTable
import ir.speaking.feature.word.progress.model.WordProgress
import ir.speaking.feature.word.word.DailyWordTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*

object WordProgressTable : UUIDTable("word_progress") {
    val userId = uuid("user_id").references(UserTable.id)
    val wordId = uuid("word_id").references(DailyWordTable.id)
    val selectedOptionIndex = integer("selected_option_index")
    val isCorrect = bool("is_correct")
    val score = integer("score")
    val answeredAt = datetime("answered_at")
}

class WordProgressDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<WordProgressDAO>(WordProgressTable)

    var userId by WordProgressTable.userId
    var wordId by WordProgressTable.wordId
    var selectedOptionIndex by WordProgressTable.selectedOptionIndex
    var isCorrect by WordProgressTable.isCorrect
    var score by WordProgressTable.score
    var answeredAt by WordProgressTable.answeredAt

    fun toModel() = WordProgress(
        uid = id.value,
        userId = userId,
        wordId = wordId,
        selectedOptionIndex = selectedOptionIndex,
        isCorrect = isCorrect,
        score = score,
        answeredAt = answeredAt,
    )
}