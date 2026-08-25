package ir.aispeaking.englishLevel.utils

import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.englishLevel.model.Question
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.uuid.Uuid


object QuestionUtils {

    fun getAllQuestions(): ImmutableList<Question> {
        return listOf(
            Question(
                question = "Choose the correct greeting: 'Hello, how _____ you?'",
                options = listOf("is", "are", "am", "be", "I don't know :("),
                answer = 1,
                weight = 1
            ),
            Question(
                question = "What is the plural of 'child'?",
                options = listOf("Childs", "Childes", "Children", "Childrens", "I don't know :("),
                answer = 2,
                weight = 1
            ),
            Question(
                question = "Choose the correct sentence: 'I _____ a student.'",
                options = listOf("am", "is", "are", "be", "I don't know :("),
                answer = 0,
                weight = 1
            ),
            Question(
                question = "Which is a color?",
                options = listOf("Apple", "Red", "Run", "Happy", "I don't know :("),
                answer = 1,
                weight = 1
            ),
            Question(
                question = "What is the opposite of 'cold'?",
                options = listOf("Hot", "Wet", "Cool", "Dry", "I don't know :("),
                answer = 0,
                weight = 1
            ),
            Question(
                question = "Choose the correct article: '_____ apple a day keeps the doctor away.'",
                options = listOf("A", "An", "The", "No article", "I don't know :("),
                answer = 1,
                weight = 1
            ),

            // Weight 2: Elementary Vocabulary and Tenses (A2-B1)
            Question(
                question = "She _____ TV every evening.",
                options = listOf("watch", "watches", "watching", "watched", "I don't know :("),
                answer = 1,
                weight = 2
            ),
            Question(
                question = "Choose the opposite of 'happy':",
                options = listOf("Sad", "Big", "Fast", "Hot", "I don't know :("),
                answer = 0,
                weight = 2
            ),
            Question(
                question = "What does 'breakfast' mean?",
                options = listOf("First meal of the day", "Last meal of the day", "A type of exercise", "A vehicle", "I don't know :("),
                answer = 0,
                weight = 2
            ),
            Question(
                question = "Yesterday, I _____ to the store.",
                options = listOf("go", "went", "goes", "going", "I don't know :("),
                answer = 1,
                weight = 2
            ),
            Question(
                question = "I usually _____ up at 7 AM.",
                options = listOf("wake", "wakes", "waking", "woke", "I don't know :("),
                answer = 0,
                weight = 2
            ),
            Question(
                question = "Which one is a day of the week?",
                options = listOf("January", "Sunday", "Winter", "Night", "I don't know :("),
                answer = 1,
                weight = 2
            ),

            // Weight 3: Intermediate Grammar and Idioms (B1-B2)
            Question(
                question = "If it rains, we _____ stay home.",
                options = listOf("will", "would", "can", "could", "I don't know :("),
                answer = 0,
                weight = 3
            ),
            Question(
                question = "Choose the synonym for 'quick':",
                options = listOf("Slow", "Fast", "Heavy", "Small", "I don't know :("),
                answer = 1,  // "Fast" - درست
                weight = 3
            ),
            Question(
                question = "What does 'kick the bucket' mean idiomatically?",
                options = listOf("To die", "To play soccer", "To clean a bucket", "To start running", "I don't know :("),
                answer = 0,
                weight = 3
            ),
            Question(
                question = "She has been living here _____ 2010.",
                options = listOf("for", "since", "at", "on", "I don't know :("),
                answer = 1,
                weight = 3
            ),
            Question(
                question = "If I _____ time, I would help you.",
                options = listOf("have", "had", "will have", "has", "I don't know :("),
                answer = 1,
                weight = 3
            ),
            Question(
                question = "What does 'piece of cake' mean idiomatically?",
                options = listOf("A slice of dessert", "Something easy", "Something expensive", "A difficult task", "I don't know :("),
                answer = 1,
                weight = 3
            ),

            // Weight 4: Advanced Vocabulary and Complex Structures (B2-C1)
            Question(
                question = "The meeting was postponed _____ the bad weather.",
                options = listOf("because", "due to", "although", "despite", "I don't know :("),
                answer = 1,
                weight = 4
            ),
            Question(
                question = "Choose the closest meaning to 'meticulous':",
                options = listOf("Careless", "Very careful", "Quick", "Lazy", "I don't know :("),
                answer = 1,
                weight = 4
            ),
            Question(
                question = "By the time we arrive, the movie _____.",
                options = listOf("will start", "will have started", "starts", "started", "I don't know :("),
                answer = 1,
                weight = 4
            ),
            Question(
                question = "He suggested that we _____ earlier.",
                options = listOf("leave", "left", "leaving", "to leave", "I don't know :("),
                answer = 0,
                weight = 4
            ),
            Question(
                question = "Not only _____ late, but he also forgot the documents.",
                options = listOf("he arrived", "did he arrive", "he did arrive", "arrived he", "I don't know :("),
                answer = 1,
                weight = 4
            ),
            Question(
                question = "Choose the correct sentence:",
                options = listOf(
                    "Had I known, I would come.",
                    "If I knew, I had come.",
                    "Had I known, I would have come.",
                    "If I have known, I would came.",
                    "I don't know :("
                ),
                answer = 2,
                weight = 4
            ),

            // Weight 5: Proficient/Academic Language (C1-C2)
            Question(
                question = "In formal writing, replace 'a lot of' with:",
                options = listOf("Numerous", "Plenty", "Bunches", "Loads", "I don't know :("),
                answer = 0,
                weight = 5
            ),
            Question(
                question = "The hypothesis was _____ by the data.",
                options = listOf("confirmed", "proved", "substantiated", "verified", "I don't know :("),
                answer = 2,
                weight = 5
            ),
            Question(
                question = "Despite the challenges, the project was completed _____ time.",
                options = listOf("in", "on", "at", "by", "I don't know :("),
                answer = 1,
                weight = 5
            ),
            Question(
                question = "Which phrase is most idiomatic in academic context: 'This implies that' or 'This means that'?",
                options = listOf("This means that", "This implies that", "This says that", "This tells that", "I don't know :("),
                answer = 1,
                weight = 5
            ),
            Question(
                question = "What is the best academic synonym for 'important'?",
                options = listOf("Cool", "Big", "Significant", "Interesting", "I don't know :("),
                answer = 2,
                weight = 5
            ),
            Question(
                question = "Choose the grammatically correct and formal sentence:",
                options = listOf(
                    "He don’t like it.",
                    "He doesn't likes it.",
                    "He does not like it.",
                    "He not like it.",
                    "I don't know :("
                ),
                answer = 2,  // درست
                weight = 5
            )
        ).toImmutableList()
    }

    fun getBalancedQuestionsPerWeight(
        allQuestions: List<Question> = getAllQuestions(),
        questionsPerWeight: Int = 2,
    ): List<Question> {
        return allQuestions
            .groupBy { it.weight }
            .flatMap { (_, questions) ->
                questions.shuffled().take(questionsPerWeight)
            }.shuffled()
    }

    /**
     * Calculates the English level based on the provided questions (subset shown to user) and their answers.
     * Scoring: Correct answer gives full weight points. Max score is sum of weights of provided questions.
     * Level thresholds are based on percentage of max score achieved, adjusted for better granularity.
     * This ensures fairness even with random question subsets, as it normalizes to the selected questions' difficulty.
     */
    fun calculateEnglishLevel(questions: List<Question>, answers: Map<Uuid, Int>): LanguageLevel {
        var totalScore = 0
        var maxPossibleScore = 0

        questions.forEach { question ->
            maxPossibleScore += question.weight
            answers[question.id]?.let { userAnswer ->
                if (userAnswer == question.answer) {
                    totalScore += question.weight
                }
                // Note: "I don't know" or wrong answers give 0 for that question
            } ?: run {
                // CHANGE: اگر جوابی برای سوال نبود، ۰ امتیاز (برای ایمنی، اگر map ناقص باشه)
            }
        }

        if (maxPossibleScore == 0) return LanguageLevel.A1 // Edge case: no questions

        val percentage = (totalScore.toDouble() / maxPossibleScore.toDouble()) * 100

        return when {
            percentage >= 95 -> LanguageLevel.C2
            percentage >= 85 -> LanguageLevel.C1
            percentage >= 70 -> LanguageLevel.B2
            percentage >= 50 -> LanguageLevel.B1
            percentage >= 30 -> LanguageLevel.A2
            else -> LanguageLevel.A1
        }
    }
}