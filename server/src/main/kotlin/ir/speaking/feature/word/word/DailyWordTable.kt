package ir.speaking.feature.word.word

import ir.speaking.feature.word.word.model.DailyWord
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*

object DailyWordTable : UUIDTable("daily_word") {
    val word = varchar("word", 50)
    val options = array<String>("options")
    val answerIndex = integer("answer_index")
    val points = integer("points")
    val createdAt = datetime("created_at")
    val showDate = datetime("show_date")
}

class DailyWordDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DailyWordDAO>(DailyWordTable)

    var word by DailyWordTable.word
    var options by DailyWordTable.options
    var answerIndex by DailyWordTable.answerIndex
    var createdAt by DailyWordTable.createdAt
    var points by DailyWordTable.points
    var expiredAt by DailyWordTable.showDate

}


fun DailyWordDAO.toModel() = DailyWord(
    uid = id.value,
    word = word,
    options = options,
    answerIndex = answerIndex,
    createdAt = createdAt.toJavaLocalDateTime(),
    points = points,
    expiredAt = expiredAt.toJavaLocalDateTime()
)
