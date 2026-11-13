package ir.speaking.feature.placement.service

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.placement.db.PlacementQuestionsTable
import ir.speaking.feature.placement.db.PlacementSessionsTable
import ir.speaking.feature.placement.dto.PlacementEvaluationResponseServerDto
import ir.speaking.feature.placement.dto.PlacementQuestionServerDto
import ir.speaking.feature.placement.dto.PlacementQuestionsResponseServerDto
import ir.speaking.feature.placement.dto.PlacementSubmitRequestServerDto
import ir.speaking.feature.placement.dto.QuestionReviewServerDto
import ir.speaking.feature.user.db.UserTable
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single
import java.util.UUID

@Single
class PlacementEvaluationService {

    private val levels = listOf("A1", "A2", "B1", "B2", "C1")

    suspend fun generateSession(userId: String = "guest"): PlacementQuestionsResponseServerDto = suspendTransaction {
        val selectedQuestions = mutableListOf<PlacementQuestionServerDto>()
        val selectedQuestionIds = mutableListOf<String>()

        for (lvl in levels) {
            val levelQuestions = PlacementQuestionsTable.selectAll()
                .where { PlacementQuestionsTable.level eq lvl }
                .toList()
                .shuffled()
                .take(4)

            for (row in levelQuestions) {
                val qId = row[PlacementQuestionsTable.id]
                val qText = row[PlacementQuestionsTable.questionText]
                val rawOptions = try {
                    Json.decodeFromString<List<String>>(row[PlacementQuestionsTable.optionsJson])
                } catch (_: Exception) {
                    emptyList()
                }
                val shuffledOptions = rawOptions.shuffled()
                val category = row[PlacementQuestionsTable.category]

                selectedQuestionIds.add(qId)
                selectedQuestions.add(
                    PlacementQuestionServerDto(
                        id = qId,
                        questionText = qText,
                        options = shuffledOptions,
                        level = lvl,
                        category = category
                    )
                )
            }
        }

        // Shuffle overall questions order or keep them progressively from A1 to C1
        // Progressive order from A1 to C1 gives the best testing experience
        val sessionId = "sess_${UUID.randomUUID()}"
        val questionIdsJson = Json.encodeToString(selectedQuestionIds)

        PlacementSessionsTable.insert {
            it[id] = sessionId
            it[PlacementSessionsTable.userId] = userId
            it[PlacementSessionsTable.questionIdsJson] = questionIdsJson
            it[scorePercent] = 0
            it[correctCount] = 0
            it[wrongCount] = 0
            it[isCompleted] = false
        }

        PlacementQuestionsResponseServerDto(
            sessionId = sessionId,
            totalQuestions = selectedQuestions.size,
            questions = selectedQuestions
        )
    }

    suspend fun evaluateSession(
        userId: String?,
        submission: PlacementSubmitRequestServerDto
    ): PlacementEvaluationResponseServerDto = suspendTransaction {
        val sessionRow = PlacementSessionsTable.selectAll()
            .where { PlacementSessionsTable.id eq submission.sessionId }
            .singleOrNull()

        val questionIds: List<String> = if (sessionRow != null) {
            try {
                Json.decodeFromString(sessionRow[PlacementSessionsTable.questionIdsJson])
            } catch (_: Exception) {
                submission.answers.keys.toList()
            }
        } else {
            submission.answers.keys.toList()
        }

        val questionRows = PlacementQuestionsTable.selectAll()
            .where { PlacementQuestionsTable.id inList questionIds }
            .associateBy { it[PlacementQuestionsTable.id] }

        var correctCount = 0
        var wrongCount = 0
        val correctPerLevel = mutableMapOf<String, Int>()
        val reviewItems = mutableListOf<QuestionReviewServerDto>()

        for (qId in questionIds) {
            val qRow = questionRows[qId]
            if (qRow != null) {
                val qText = qRow[PlacementQuestionsTable.questionText]
                val qLvl = qRow[PlacementQuestionsTable.level]
                val correctOpt = qRow[PlacementQuestionsTable.correctOption]
                val options = try {
                    Json.decodeFromString<List<String>>(qRow[PlacementQuestionsTable.optionsJson])
                } catch (_: Exception) {
                    emptyList()
                }

                val userSelected = submission.answers[qId]
                val isCorrect = userSelected != null && userSelected.trim().equals(correctOpt.trim(), ignoreCase = true)

                if (isCorrect) {
                    correctCount++
                    correctPerLevel[qLvl] = (correctPerLevel[qLvl] ?: 0) + 1
                } else {
                    wrongCount++
                }

                reviewItems.add(
                    QuestionReviewServerDto(
                        questionId = qId,
                        questionText = qText,
                        options = options,
                        selectedOption = userSelected,
                        correctOption = correctOpt,
                        isCorrect = isCorrect,
                        level = qLvl
                    )
                )
            }
        }

        val totalCount = questionIds.size.coerceAtLeast(1)
        val scorePercent = ((correctCount.toDouble() / totalCount) * 100).toInt()

        // CEFR Level Calculation
        val assignedLevel = when {
            scorePercent >= 85 && (correctPerLevel["C1"] ?: 0) >= 3 -> "C1"
            scorePercent >= 70 && (correctPerLevel["B2"] ?: 0) >= 3 -> "B2"
            scorePercent >= 55 && (correctPerLevel["B1"] ?: 0) >= 2 -> "B1"
            scorePercent >= 35 && (correctPerLevel["A2"] ?: 0) >= 2 -> "A2"
            else -> "A1"
        }

        val (levelTitleFa, descriptionFa) = when (assignedLevel) {
            "C1" -> Pair(
                "پیشرفته (Advanced - C1)",
                "تبریک! تسلط زبانی شما بسیار عالی و نزدیک به گویشوران بومی است. شما به راحتی پیچیده‌ترین مکالمات را متوجه می‌شوید."
            )
            "B2" -> Pair(
                "بالاتر از متوسط (Upper-Intermediate - B2)",
                "تسلط شما بسیار خوب است. توانایی بحث و تبادل نظر پیرامون موضوعات تخصصی و انتزاعی را با اعتماد به نفس کامل دارید."
            )
            "B1" -> Pair(
                "متوسط (Intermediate - B1)",
                "شما توانایی برقراری مکالمات روان در موضوعات سفر، کار و محیط‌های روزمره را دارید و به خوبی مفهوم خود را می‌رسانید."
            )
            "A2" -> Pair(
                "مقدماتی (Elementary - A2)",
                "شما پایه‌های زبان انگلیسی را به خوبی متوجه می‌شوید و در شرایط ساده و روزمره می‌توانید ارتباط برقرار کنید."
            )
            else -> Pair(
                "مبتدی (Beginner - A1)",
                "شما در ابتدای مسیر یادگیری مکالمه انگلیسی هستید. داستان‌های قدم به قدم ما شما را به سطوح بالاتر هدایت خواهند کرد."
            )
        }

        // Update session
        if (sessionRow != null) {
            PlacementSessionsTable.update({ PlacementSessionsTable.id eq submission.sessionId }) {
                it[PlacementSessionsTable.scorePercent] = scorePercent
                it[PlacementSessionsTable.assignedLevel] = assignedLevel
                it[PlacementSessionsTable.correctCount] = correctCount
                it[PlacementSessionsTable.wrongCount] = wrongCount
                it[PlacementSessionsTable.isCompleted] = true
                it[PlacementSessionsTable.completedAt] = Clock.System.now()
            }
        }

        // Update user's profile level if user is logged in
        if (!userId.isNullOrBlank() && userId != "guest") {
            try {
                val userUuid = UUID.fromString(userId)
                UserTable.update({ UserTable.id eq userUuid }) {
                    it[languageLevel] = assignedLevel
                }
            } catch (_: Exception) {}
        }

        PlacementEvaluationResponseServerDto(
            sessionId = submission.sessionId,
            assignedLevel = assignedLevel,
            scorePercent = scorePercent,
            correctCount = correctCount,
            wrongCount = wrongCount,
            totalCount = totalCount,
            levelTitleFa = levelTitleFa,
            descriptionFa = descriptionFa,
            reviewItems = reviewItems
        )
    }
}
