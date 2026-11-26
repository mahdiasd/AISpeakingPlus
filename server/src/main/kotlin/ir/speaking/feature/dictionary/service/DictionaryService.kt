package ir.speaking.feature.dictionary.service

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import ir.speaking.core.network.api.AiApiService
import ir.speaking.core.redis.di.RedisClientHolder
import ir.speaking.feature.dictionary.dto.AiDictionaryResult
import ir.speaking.feature.dictionary.dto.DictionaryLookupResponse
import ir.speaking.feature.dictionary.model.LexiconEntry
import ir.speaking.feature.dictionary.repository.LexiconRepository
import ir.speaking.feature.dictionary.validation.InvalidWordException
import ir.speaking.feature.dictionary.validation.WordInputValidator
import ir.speaking.feature.tts.service.TtsService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import org.redisson.api.RBucket
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Single
open class DictionaryService(
    private val lexiconRepository: LexiconRepository,
    private val aiApiService: AiApiService,
    private val ttsService: TtsService,
    redisClientHolder: RedisClientHolder
) {
    private val logger = LoggerFactory.getLogger(DictionaryService::class.java)
    private val redissonClient: RedissonClient? = redisClientHolder.client
    private val inMemoryCache = ConcurrentHashMap<String, DictionaryLookupResponse>()

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    companion object {
        private const val REDIS_PREFIX = "dict:lookup:v2:"
        private const val CACHE_TTL_DAYS = 30L

        private const val SYSTEM_PROMPT = """You are an expert bilingual lexicographer and dictionary service for English language learners (Persian speakers).

STEP 1 — VALIDATION GATE (MANDATORY BEFORE ANY ANALYSIS):
First evaluate whether the input is a real, meaningful, appropriate English word, phrasal verb, idiom, collocation, or common English expression suitable for a language learner's flashcard deck.
You MUST set "isValid": false and provide a short, polite Persian explanation in "rejectionReasonFa" (and MUST NOT invent meanings or translations) if the input is ANY of the following:
1. Gibberish, random characters, keyboard smash, or nonsensical text (e.g., "asdfgh", "xyz", "qwert", "blabla").
2. Finglish/Pinglish (Persian written in Latin letters) or non-English words/phrases (e.g., "salam", "chetori", "khobi", "abbas bo azar").
3. Random personal names or meaningless combinations of names/words with no English dictionary meaning (e.g., "abbas bo azar", "ali reza").
4. Profanity, vulgar/obscene/sexual slurs, offensive insults, hate speech, or inappropriate content.
5. Made-up or severely misspelled non-words that are not recognizable English vocabulary.

If invalid, output ONLY:
{"isValid": false, "rejectionReasonFa": "<short polite Persian explanation>"}

STEP 2 — LEXICOGRAPHICAL ANALYSIS (ONLY IF VALID):
Analyze the given English word, phrase, idiom, or expression within its context (if provided) and output a strict JSON object with:
- "isValid": true
- "rejectionReasonFa": null
- "headword": the exact word, phrase, idiom, or expression being analyzed (e.g. "Hello, I am Rachel", "take care of", "withdraw", "apple"). Preserve multi-word phrases and expressions exactly as requested by the user, without reducing or truncating them to a single word lemma!
- "phonetic": accurate IPA phonetic transcription (e.g. "/hə'loʊ/"). Use standard ASCII apostrophe "'" for primary stress to guarantee font compatibility across all devices.
- "partOfSpeech": grammatical category (e.g. "phrase", "expression", "sentence", "noun", "verb", "adjective", "adverb", "phrasal verb", "idiom")
- "contextMeaningFa": natural, accurate Persian translation matching the provided context (or primary meaning if no context)
- "definitionEn": simple, clear English learner definition
- "otherMeaningsFa": list of 1 to 4 other common Persian translations (array of strings)
- "sampleSentenceEn": a natural English example sentence illustrating the usage
- "sampleSentenceFa": accurate Persian translation of the sample sentence

CRITICAL: Output ONLY valid, parseable JSON conforming to this schema. Do not include markdown codeblocks, explanations, or additional text."""
    }

    open fun normalize(raw: String): String {
        return raw.trim().lowercase().replace(Regex("[^a-zA-Z0-9'\\-\\s]"), "").trim()
    }

    open suspend fun lookup(rawWord: String, contextSentence: String? = null): DictionaryLookupResponse {
        val validation = WordInputValidator.validate(rawWord)
        if (!validation.isValid) {
            throw InvalidWordException(validation.reasonFa ?: "عبارت واردشده معتبر نیست.")
        }

        val normalized = normalize(rawWord)
        if (normalized.isBlank()) {
            throw InvalidWordException("لطفاً یک کلمه یا اصطلاح انگلیسی وارد کنید.")
        }

        // 1. Check Redis cache (or in-memory fallback)
        val cached = getFromCache(normalized)
        if (cached != null) {
            // Guardrail: if normalized query is a multi-word phrase but cached headword was truncated to a single word, invalidate
            if (normalized.contains(" ") && !cached.headword.contains(" ")) {
                logger.info("Evicting outdated single-word cached entry '${cached.headword}' for multi-word phrase '$normalized'")
                evictFromCache(normalized)
            } else {
                logger.debug("Dictionary cache hit for '$normalized'")
                return cached
            }
        }

        // 2. Check Database (LexiconRepository)
        val dbEntry = lexiconRepository.findByNormalizedText(normalized)
        if (dbEntry != null) {
            if (normalized.contains(" ") && !dbEntry.headword.contains(" ")) {
                logger.info("Found outdated single-word headword '${dbEntry.headword}' in DB for phrase '$normalized'. Re-analyzing with AI.")
                lexiconRepository.deleteByNormalizedText(normalized)
            } else {
                val response = ensureAudioAndMap(dbEntry)
                putToCache(normalized, response)
                return response
            }
        }

        // 3. Cache Miss: Query 9router AI model via AiApiService
        val aiResult = queryAiDictionary(normalized, rawWord, contextSentence)

        if (!aiResult.isValid || aiResult.contextMeaningFa.isBlank() || aiResult.headword.isBlank()) {
            throw InvalidWordException(
                aiResult.rejectionReasonFa?.takeIf { it.isNotBlank() }
                    ?: "عبارت «${rawWord.trim()}» یک کلمه یا اصطلاح معتبر انگلیسی نیست و قابل افزودن به لایتنر نمی‌باشد."
            )
        }

        // 4. Synthesize pronunciation audio via Kokoro TTS
        val effectiveHeadword = aiResult.headword.trim()
        val audioUrl = synthesizeWordAudio(effectiveHeadword)

        // 5. Persist to Database
        val otherMeaningsJson = if (aiResult.otherMeaningsFa.isNotEmpty()) {
            jsonParser.encodeToString(aiResult.otherMeaningsFa)
        } else {
            null
        }

        val savedEntry = lexiconRepository.save(
            normalizedText = normalized,
            headword = effectiveHeadword,
            phonetic = aiResult.phonetic,
            partOfSpeech = aiResult.partOfSpeech,
            contextMeaningFa = aiResult.contextMeaningFa,
            otherMeaningsJson = otherMeaningsJson,
            definitionEn = aiResult.definitionEn,
            sampleSentenceEn = aiResult.sampleSentenceEn,
            sampleSentenceFa = aiResult.sampleSentenceFa,
            audioUrl = audioUrl
        )

        val response = DictionaryLookupResponse(
            id = savedEntry.id,
            headword = savedEntry.headword,
            phonetic = savedEntry.phonetic,
            partOfSpeech = savedEntry.partOfSpeech,
            contextMeaningFa = savedEntry.contextMeaningFa,
            otherMeaningsFa = aiResult.otherMeaningsFa,
            definitionEn = savedEntry.definitionEn,
            sampleSentenceEn = savedEntry.sampleSentenceEn,
            sampleSentenceFa = savedEntry.sampleSentenceFa,
            audioUrl = savedEntry.audioUrl
        )

        // 6. Cache and return
        putToCache(normalized, response)
        return response
    }

    private suspend fun queryAiDictionary(
        normalized: String,
        rawWord: String,
        contextSentence: String?
    ): AiDictionaryResult {
        val userContent = buildString {
            append("Word or Phrase to Analyze: \"$rawWord\"\n")
            if (!contextSentence.isNullOrBlank()) {
                append("Context Sentence: \"$contextSentence\"")
            } else {
                append("Context Sentence: None")
            }
        }

        val messages = listOf(
            ChatMessage(role = ChatRole.System, content = SYSTEM_PROMPT),
            ChatMessage(role = ChatRole.User, content = userContent)
        )

        val result = aiApiService.requestRaw(
            messages = messages,
            responseFormat = ChatResponseFormat.JsonObject
        )

        val rawJson = result.getOrThrow()
        val cleanJson = sanitizeJson(rawJson)

        return try {
            jsonParser.decodeFromString<AiDictionaryResult>(cleanJson)
        } catch (e: Exception) {
            logger.warn("Failed to parse AI dictionary JSON: '$cleanJson', error: ${e.message}")
            throw IllegalStateException("Failed to parse AI dictionary response", e)
        }
    }

    open suspend fun validateWordWithAiIfAvailable(rawWord: String) {
        val validation = WordInputValidator.validate(rawWord)
        if (!validation.isValid) {
            throw InvalidWordException(validation.reasonFa ?: "عبارت واردشده معتبر نیست.")
        }

        val normalized = normalize(rawWord)
        if (normalized.isBlank()) {
            throw InvalidWordException("لطفاً یک کلمه یا اصطلاح انگلیسی وارد کنید.")
        }

        if (getFromCache(normalized) != null || lexiconRepository.findByNormalizedText(normalized) != null) {
            return
        }

        val aiResult = try {
            queryAiDictionary(normalized, rawWord, null)
        } catch (e: InvalidWordException) {
            throw e
        } catch (e: Exception) {
            logger.warn("AI validation unavailable for '$rawWord', skipping AI check: ${e.message}")
            return
        }

        if (!aiResult.isValid || aiResult.contextMeaningFa.isBlank() || aiResult.headword.isBlank()) {
            throw InvalidWordException(
                aiResult.rejectionReasonFa?.takeIf { it.isNotBlank() }
                    ?: "عبارت «${rawWord.trim()}» یک کلمه یا اصطلاح معتبر انگلیسی نیست و قابل افزودن به لایتنر نمی‌باشد."
            )
        }
    }

    private fun sanitizeJson(text: String): String {
        var cleaned = text.trim()
        if (cleaned.startsWith("```json", ignoreCase = true)) {
            cleaned = cleaned.substring(7)
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3)
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length - 3)
        }
        cleaned = cleaned.trim()

        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        return if (start != -1 && end != -1 && end > start) {
            cleaned.substring(start, end + 1)
        } else {
            cleaned
        }
    }

    open suspend fun synthesizeWordAudio(headword: String): String? {
        val safeLemma = headword.trim().lowercase().replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(60)
        if (safeLemma.isBlank()) return null

        val wordsDir = File("audio_cache/words")
        if (!wordsDir.exists()) {
            wordsDir.mkdirs()
        }

        val targetFile = File(wordsDir, "$safeLemma.mp3")
        val relativeUrl = "/audio_cache/words/$safeLemma.mp3"

        if (targetFile.exists() && targetFile.length() > 44) {
            return relativeUrl
        }

        return try {
            val ttsResult = ttsService.synthesize(headword)
            if (ttsResult != null) {
                val bytes = ttsResult.audioData ?: run {
                    ttsService.getAudioFile(ttsResult.filename)?.readBytes()
                }
                if (bytes != null && bytes.isNotEmpty()) {
                    targetFile.writeBytes(bytes)
                    return relativeUrl
                }

                val srcFile = ttsService.getAudioFile(ttsResult.filename)
                if (srcFile != null && srcFile.exists()) {
                    srcFile.copyTo(targetFile, overwrite = true)
                    return relativeUrl
                }

                ttsResult.audioUrl
            } else {
                null
            }
        } catch (e: Exception) {
            logger.warn("Audio synthesis for word '$headword' failed: ${e.message}")
            null
        }
    }

    private suspend fun ensureAudioAndMap(dbEntry: LexiconEntry): DictionaryLookupResponse {
        var audioUrl = dbEntry.audioUrl
        if (audioUrl.isNullOrBlank()) {
            val generated = synthesizeWordAudio(dbEntry.headword)
            if (generated != null) {
                audioUrl = generated
                lexiconRepository.updateAudioUrl(UUID.fromString(dbEntry.id), generated)
            }
        }

        val otherMeanings = dbEntry.otherMeaningsJson?.let {
            try {
                jsonParser.decodeFromString<List<String>>(it)
            } catch (_: Exception) {
                emptyList()
            }
        } ?: emptyList()

        return DictionaryLookupResponse(
            id = dbEntry.id,
            headword = dbEntry.headword,
            phonetic = dbEntry.phonetic,
            partOfSpeech = dbEntry.partOfSpeech,
            contextMeaningFa = dbEntry.contextMeaningFa,
            otherMeaningsFa = otherMeanings,
            definitionEn = dbEntry.definitionEn,
            sampleSentenceEn = dbEntry.sampleSentenceEn,
            sampleSentenceFa = dbEntry.sampleSentenceFa,
            audioUrl = audioUrl
        )
    }

    private fun getFromCache(normalized: String): DictionaryLookupResponse? {
        val client = redissonClient
        if (client != null) {
            try {
                val bucket: RBucket<String> = client.getBucket(REDIS_PREFIX + normalized)
                val json = bucket.get()
                if (!json.isNullOrBlank()) {
                    return jsonParser.decodeFromString<DictionaryLookupResponse>(json)
                }
            } catch (e: Exception) {
                logger.warn("Redis read error for key '$normalized': ${e.message}")
            }
        }
        return inMemoryCache[normalized]
    }

    private fun putToCache(normalized: String, response: DictionaryLookupResponse) {
        val client = redissonClient
        if (client != null) {
            try {
                val bucket: RBucket<String> = client.getBucket(REDIS_PREFIX + normalized)
                val json = jsonParser.encodeToString(response)
                bucket.set(json, Duration.ofDays(CACHE_TTL_DAYS))
            } catch (e: Exception) {
                logger.warn("Redis write error for key '$normalized': ${e.message}")
            }
        }
        inMemoryCache[normalized] = response
    }

    private fun evictFromCache(normalized: String) {
        val client = redissonClient
        if (client != null) {
            try {
                val bucket: RBucket<String> = client.getBucket(REDIS_PREFIX + normalized)
                bucket.delete()
            } catch (e: Exception) {
                logger.warn("Redis delete error for key '$normalized': ${e.message}")
            }
        }
        inMemoryCache.remove(normalized)
    }
}
