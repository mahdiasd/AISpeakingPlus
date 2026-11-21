package ir.aispeaking.domain.model.leitner

import kotlinx.serialization.Serializable

@Serializable
enum class LeitnerBox(
    val boxNumber: Int,
    val intervalDays: Int,
    val titleFa: String
) {
    BOX_1(1, 1, "جعبه ۱ (۱ روزه)"),
    BOX_2(2, 3, "جعبه ۲ (۳ روزه)"),
    BOX_3(3, 7, "جعبه ۳ (۷ روزه)"),
    BOX_4(4, 14, "جعبه ۴ (۱۴ روزه)"),
    BOX_5(5, 30, "جعبه ۵ (۳۰ روزه)");

    companion object {
        fun fromBoxNumber(number: Int): LeitnerBox = entries.find { it.boxNumber == number } ?: BOX_1
    }
}

@Serializable
data class LeitnerBoxStats(
    val box: LeitnerBox,
    val totalCards: Long = 0,
    val dueCards: Long = 0
)
