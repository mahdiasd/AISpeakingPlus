# Design: Smart Contextual Dictionary & Spaced Repetition Leitner Box

## Context

See [proposal.md](file:///Users/mahdi/StudioProjects/AISpeakingPlus/openspec/changes/add-leitner-and-dictionary/proposal.md) for background and motivation.
The client is a Kotlin Multiplatform app using Compose Multiplatform targeting Android, iOS, Desktop, and Web (Wasm/JS). The backend is built with Ktor 3, PostgreSQL (Exposed ORM), Redis, Kokoro TTS, and 9router (OpenAI-compatible router for LLM completions on port 20128).

## Goals / Non-Goals

**Goals:**
- Provide a responsive in-chat word and phrase lookup with sub-100ms response for cached entries.
- Implement an interactive tokenized word picker avoiding cumbersome native OS selection handles.
- Store vocabulary in a global shared lexicon cache (`lexicon_entries`) and link it to individual user decks (`user_flashcards`).
- Deliver a tactile 5-box Leitner review interface reusing the existing 3D coverflow carousel mechanics from `SubscriptionScreen`.
- Support configurable daily push notifications with a zero-keyboard numeric stepper.

**Non-Goals:**
- Offline LLM inference (all LLM translation is handled server-side via 9router).
- Complex SuperMemo SM-2 / Anki FSRS ease-factor formulas (using straightforward 5-box intervals).
- Full speech recognition (STT) assessment during Leitner review (audio is for listening only in this phase).

## Decisions

### 1. Interactive Word-Token Sheet vs Native Selection Handles
- **Decision**: Parse message text into whitespace-delimited tokens and display them as clickable interactive chips (`WordTokenSelectorSheet`).
- **Rationale**: Selecting text on mobile touch screens or inside Canvas/Wasm Compose web apps is often imprecise, especially when attempting to select a 1-2 word phrase. Tokenized chips provide instantaneous, unambiguous selection with clear visual feedback in Serene Cyan.
- **Alternatives Considered**:
  - *Native selection via `SelectionContainer`*: Clashes with parent scroll and gesture interceptors, difficult to control programmatically across platforms.
  - *Regex/Inline clickable spans inside bubble*: Clutters chat item rendering and risks accidental clicks while attempting to scroll chat.

### 2. Two-Tier Lexicon Cache & 9router Integration
- **Decision**: Implement a shared `lexicon_entries` table in PostgreSQL cached by normalized query string in Redis. Only cache misses trigger the 9router Dictionary combo.
- **Rationale**: Common words like "withdraw", "explore", or "appointment" will be queried by multiple users. A global cache drastically cuts LLM latency from ~1200ms to <50ms and saves API tokens.
- **Prompt & JSON Contract for 9router**:
  ```json
  {
    "headword": "withdraw",
    "phonetic": "/wɪðˈdrɔː/",
    "partOfSpeech": "verb",
    "contextMeaningFa": "برداشت کردن (پول)",
    "definitionEn": "to take money out of an account",
    "otherMeaningsFa": ["عقب‌نشینی کردن", "صرف‌نظر کردن"],
    "sampleSentenceEn": "I need to withdraw some cash from the ATM.",
    "sampleSentenceFa": "باید مقداری پول نقد از دستگاه خودپرداز برداشت کنم."
  }
  ```
- **Alternatives Considered**: Direct dictionary call on every lookup (rejected: high latency, redundant LLM costs).

### 3. Automatic Audio Synthesis via Kokoro TTS
- **Decision**: Upon first lookup of any word, Ktor calls `TtsService` (Kokoro) to synthesize audio, writing it to `audio_cache/words/{lemma}.mp3` and returning a persistent static URL.
- **Rationale**: Eliminates repetitive TTS synthesis load and guarantees instant offline-capable playback.

### 4. Spaced Repetition Progression (5 Boxes, 1-Step Back Penalty)
- **Decision**: 
  - Box 1: Day 1 (Every day)
  - Box 2: Day 3
  - Box 3: Day 7
  - Box 4: Day 14
  - Box 5: Day 30 (Mastered)
  - Failure penalty: Drop exactly 1 box (e.g., Box 4 -> Box 3; Box 1 stays in Box 1).
- **Rationale**: Harsh resets back to Box 1 demotivate language learners. The 1-step penalty maintains accountability while encouraging persistent practice.

### 5. UI Architecture: Grid View + 3D Coverflow Review Pager
- **Decision**: Reuse the 3D coverflow projection formulas from `ModernPlanCard` / `SubscriptionScreen`:
  ```kotlin
  val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
  val scale = 1f - (pageOffset.absoluteValue.coerceIn(0f, 1.5f) * 0.08f)
  val alpha = 1f - (pageOffset.absoluteValue.coerceIn(0f, 1.5f) * 0.18f).coerceIn(0f, 0.28f)
  val rotationY = pageOffset * 7.5f
  ```
- **Rationale**: Reuses tested, highly polished visual patterns matching the Liquid Glass Apple design language of the app.

### 6. Database Schema Design (Exposed ORM)
```kotlin
object LexiconTable : UUIDTable("lexicon_entries") {
    val normalizedText = varchar("normalized_text", 255).uniqueIndex()
    val headword = varchar("headword", 128)
    val phonetic = varchar("phonetic", 128).nullable()
    val partOfSpeech = varchar("part_of_speech", 64).nullable()
    val contextMeaningFa = text("context_meaning_fa")
    val otherMeanings = text("other_meanings_json").nullable()
    val definitionEn = text("definition_en").nullable()
    val sampleSentenceEn = text("sample_sentence_en").nullable()
    val sampleSentenceFa = text("sample_sentence_fa").nullable()
    val audioUrl = varchar("audio_url", 512).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object UserFlashcardTable : UUIDTable("user_flashcards") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val lexiconId = reference("lexicon_id", LexiconTable, onDelete = ReferenceOption.CASCADE)
    val boxNumber = integer("box_number").default(1)
    val reviewCount = integer("review_count").default(0)
    val lastReviewedAt = timestamp("last_reviewed_at").nullable()
    val nextReviewAt = timestamp("next_review_at")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}
```

## Risks / Trade-offs

- **[Risk] Polysemy & Ambiguous Word Context**: If "bank" is cached as "بانک" from a financial chat, a user tapping "river bank" later might get the wrong cached meaning.
  - *Mitigation*: Index lexicon by `normalized_text + context_hash` when context matters, or return all meanings while highlighting the context-specific one in UI.
- **[Risk] LLM JSON Parsing Failure from 9router**: Model might return markdown fences or malformed JSON.
  - *Mitigation*: Use `json.decodeFromString` with lenient sanitization stripping ````json ... ```` fences and fallback to basic dictionary definitions.
- **[Risk] High volume of due cards overwhelming returning users**:
  - *Mitigation*: Cap each review session to a manageable maximum (e.g., 20 cards per review batch) sorted by earliest overdue timestamp.

## Migration Plan

1. **Database Migration**: Run SQL migration adding `lexicon_entries` and `user_flashcards` tables with appropriate indices.
2. **Server Deployment**: Deploy updated Ktor backend with `/api/dictionary` and `/api/leitner` endpoints; verify Kokoro audio caching on server.
3. **Client Deployment**: Ship KMP client update containing `WordTokenSelectorSheet`, `LeitnerScreen`, and Journey Map edge tab.
