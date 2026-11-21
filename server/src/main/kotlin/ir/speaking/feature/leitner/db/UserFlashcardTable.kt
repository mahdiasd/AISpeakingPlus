package ir.speaking.feature.leitner.db

import ir.speaking.feature.dictionary.db.LexiconTable
import ir.speaking.feature.user.db.UserTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UserFlashcardTable : UUIDTable("user_flashcards") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val lexiconId = reference("lexicon_id", LexiconTable, onDelete = ReferenceOption.CASCADE).nullable()
    val customWord = varchar("custom_word", 128).nullable()
    val customMeaning = text("custom_meaning").nullable()
    val customExample = text("custom_example").nullable()
    val customAudioUrl = varchar("custom_audio_url", 512).nullable()
    val boxNumber = integer("box_number").default(1)
    val reviewCount = integer("review_count").default(0)
    val lastReviewedAt = timestamp("last_reviewed_at").nullable()
    val nextReviewAt = timestamp("next_review_at")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        index("idx_user_flashcards_user_due", false, userId, nextReviewAt)
        index("idx_user_flashcards_user_box", false, userId, boxNumber)
    }
}
