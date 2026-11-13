package ir.aispeaking.profile.component

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EditNickNameDialogTest {

    @Test
    fun `isEnglishFirstWord detects English first word correctly`() {
        assertTrue(isEnglishFirstWord("John"))
        assertTrue(isEnglishFirstWord("John Doe"))
        assertTrue(isEnglishFirstWord("  Ali"))
        assertTrue(isEnglishFirstWord("Sarah احمدی"))
        assertTrue(isEnglishFirstWord("Alex_99"))
        assertTrue(isEnglishFirstWord("M"))
    }

    @Test
    fun `isEnglishFirstWord detects Persian or non-English first word correctly`() {
        assertFalse(isEnglishFirstWord("مهدی"))
        assertFalse(isEnglishFirstWord("مهدی رضایی"))
        assertFalse(isEnglishFirstWord("رضا Ali"))
        assertFalse(isEnglishFirstWord("علی"))
        assertFalse(isEnglishFirstWord(""))
        assertFalse(isEnglishFirstWord("   "))
        assertFalse(isEnglishFirstWord("123"))
        assertFalse(isEnglishFirstWord("123 John"))
    }
}
