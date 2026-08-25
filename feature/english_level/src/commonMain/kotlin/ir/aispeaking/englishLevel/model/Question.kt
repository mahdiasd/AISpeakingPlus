package ir.aispeaking.englishLevel.model

import ir.aispeaking.utils.MyUtils
import kotlin.uuid.Uuid


data class Question(
    val id: Uuid = MyUtils.generateUUID(),

    val question: String,

    val options: List<String>, // 4 item

    val answer: Int, // index of options

    val weight: Int // weight of question
)
