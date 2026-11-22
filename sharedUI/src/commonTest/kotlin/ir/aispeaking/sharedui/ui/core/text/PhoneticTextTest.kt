package ir.aispeaking.sharedui.ui.core.text

import kotlin.test.Test
import kotlin.test.assertEquals

class PhoneticTextTest {

    @Test
    fun testCleanIpaPhoneticStressMarksAndCharacters() {
        // Standard IPA from dictionary/AI with modifier vertical lines and script g
        val raw = "/ɪnˌvestɪˈɡeɪʃən/"
        val cleaned = cleanIpaPhonetic(raw)
        assertEquals("/ɪn,vestɪ'geɪʃən/", cleaned)
    }

    @Test
    fun testCleanIpaPhoneticLongVowelsAndModifiers() {
        val raw = "/ˈfluːənt/"
        val cleaned = cleanIpaPhonetic(raw)
        assertEquals("/'flu:ənt/", cleaned)
    }

    @Test
    fun testCleanIpaPhoneticEnclosesInSlashes() {
        val rawNoSlashes = "həˈloʊ"
        val cleaned = cleanIpaPhonetic(rawNoSlashes)
        assertEquals("/hə'loʊ/", cleaned)
    }

    @Test
    fun testCleanIpaPhoneticBlank() {
        assertEquals("", cleanIpaPhonetic(""))
        assertEquals("", cleanIpaPhonetic("   "))
    }
}
