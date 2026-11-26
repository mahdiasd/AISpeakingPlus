package ir.aispeaking.domain.validation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LeitnerWordValidatorTest {

    @Test
    fun validEnglishWordsAndPhrasesPass() {
        val validInputs = listOf(
            "apple",
            "breakthrough",
            "take care of",
            "don't",
            "state-of-the-art",
            "a",
            "I",
            "Hello, how are you?"
        )
        validInputs.forEach { input ->
            val result = LeitnerWordValidator.validate(input)
            assertTrue(result.isValid, "Expected '$input' to be valid, got: ${result.errorMessage}")
        }
    }

    @Test
    fun invalidGibberishNonEnglishAndProfanityFail() {
        val invalidInputs = listOf(
            "",
            "   ",
            "سلام",
            "12345",
            "abc123",
            "aaaa",
            "bcdfghj",
            "x",
            "fuck"
        )
        invalidInputs.forEach { input ->
            val result = LeitnerWordValidator.validate(input)
            assertFalse(result.isValid, "Expected '$input' to be invalid")
            assertNotNull(result.errorMessage)
        }
    }
}
