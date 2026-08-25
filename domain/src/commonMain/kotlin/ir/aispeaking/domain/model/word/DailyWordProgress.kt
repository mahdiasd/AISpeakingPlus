package ir.aispeaking.domain.model.word

data class DailyWordProgress(
    val uid: String,
    val selectedOptionIndex: Int,
    val score: Int,
    val isCorrect: Boolean,
)