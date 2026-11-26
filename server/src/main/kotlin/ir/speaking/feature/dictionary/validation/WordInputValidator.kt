package ir.speaking.feature.dictionary.validation

class InvalidWordException(message: String) : IllegalArgumentException(message)

data class WordValidationResult(
    val isValid: Boolean,
    val reasonFa: String? = null
)

object WordInputValidator {

    private val ALLOWED_CHARS_REGEX = Regex("^[a-zA-Z0-9\\s'’\\-.,!?/&]+$")
    private val HAS_LETTER_REGEX = Regex("[a-zA-Z]")
    private val REPEATED_CHAR_REGEX = Regex("([a-zA-Z])\\1{3,}")
    private val CONSONANT_CLUSTER_REGEX = Regex("[bcdfghjklmnpqrstvwxz]{6,}", RegexOption.IGNORE_CASE)
    private val VOWEL_REGEX = Regex("[aeiouy]", RegexOption.IGNORE_CASE)

    private val INAPPROPRIATE_WORDS = setOf(
        // English profanity / slurs
        "fuck", "fucking", "fucker", "fucked", "fucks", "shit", "shitty", "bullshit",
        "bitch", "bitches", "asshole", "bastard", "dick", "dickhead", "cock", "pussy",
        "cunt", "whore", "slut", "motherfucker", "nigger", "nigga", "faggot", "fag",
        "retard", "retarded", "porn", "porno", "pornography", "hentai", "blowjob",
        "handjob", "masturbate", "masturbation", "dildo", "anal", "orgasm", "erection",
        "jackass", "dumbass", "dipshit", "wanker", "twat", "prick", "bollocks",
        // Common Finglish profanity
        "koon", "kon", "kir", "kos", "kose", "koskesh", "kooni", "koni", "jende",
        "jendeh", "ghahbe", "haroomzadeh", "haramzadeh", "pedarsag", "madarjende",
        "biasharaf", "bisharaf", "ashghal", "laashi", "lashi", "goh", "gushad",
        "madarghahbe", "shash", "pofioz", "pofyuz", "dayoos", "dayus", "kiri"
    )

    fun validate(rawWord: String): WordValidationResult {
        val cleaned = rawWord.trim().replace(Regex("\\s+"), " ")
        if (cleaned.isBlank()) {
            return WordValidationResult(
                isValid = false,
                reasonFa = "لطفاً یک کلمه یا اصطلاح انگلیسی وارد کنید."
            )
        }

        if (!ALLOWED_CHARS_REGEX.matches(cleaned)) {
            return WordValidationResult(
                isValid = false,
                reasonFa = "لطفاً فقط کلمه یا اصطلاح انگلیسی وارد کنید."
            )
        }

        if (!HAS_LETTER_REGEX.containsMatchIn(cleaned)) {
            return WordValidationResult(
                isValid = false,
                reasonFa = "کلمه واردشده باید شامل حروف انگلیسی باشد."
            )
        }

        val lettersOnly = cleaned.filter { it in 'a'..'z' || it in 'A'..'Z' }
        if (lettersOnly.length < 2 && lettersOnly.lowercase() !in setOf("a", "i")) {
            return WordValidationResult(
                isValid = false,
                reasonFa = "کلمه واردشده بیش از حد کوتاه است."
            )
        }

        val tokens = cleaned.split(" ").filter { it.isNotBlank() }
        if (cleaned.length > 100 || tokens.size > 12) {
            return WordValidationResult(
                isValid = false,
                reasonFa = "عبارت واردشده بیش از حد طولانی است."
            )
        }

        var hasAlphabeticToken = false
        for (token in tokens) {
            val hasLetter = token.any { it in 'a'..'z' || it in 'A'..'Z' }
            val hasDigit = token.any { it in '0'..'9' }
            if (hasLetter && hasDigit) {
                return WordValidationResult(
                    isValid = false,
                    reasonFa = "عبارت واردشده یک کلمه یا اصطلاح معتبر انگلیسی نیست."
                )
            }
            if (hasLetter) {
                hasAlphabeticToken = true
            }
        }

        if (!hasAlphabeticToken) {
            return WordValidationResult(
                isValid = false,
                reasonFa = "عبارت واردشده یک کلمه یا اصطلاح معتبر انگلیسی نیست."
            )
        }

        if (REPEATED_CHAR_REGEX.containsMatchIn(cleaned)) {
            return WordValidationResult(
                isValid = false,
                reasonFa = "عبارت واردشده یک کلمه یا اصطلاح معتبر انگلیسی نیست."
            )
        }

        for (token in tokens) {
            val alphaToken = token.filter { it in 'a'..'z' || it in 'A'..'Z' }.lowercase()
            if (alphaToken.isEmpty()) continue

            if (alphaToken in INAPPROPRIATE_WORDS) {
                return WordValidationResult(
                    isValid = false,
                    reasonFa = "کلمه واردشده نامناسب است و امکان افزودن آن به لایتنر وجود ندارد."
                )
            }

            if (CONSONANT_CLUSTER_REGEX.containsMatchIn(alphaToken)) {
                return WordValidationResult(
                    isValid = false,
                    reasonFa = "عبارت واردشده یک کلمه یا اصطلاح معتبر انگلیسی نیست."
                )
            }

            if (alphaToken.length >= 4 && !VOWEL_REGEX.containsMatchIn(alphaToken)) {
                return WordValidationResult(
                    isValid = false,
                    reasonFa = "عبارت واردشده یک کلمه یا اصطلاح معتبر انگلیسی نیست."
                )
            }
        }

        return WordValidationResult(isValid = true)
    }
}
