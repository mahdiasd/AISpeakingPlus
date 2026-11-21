# Tasks

## 1. Backend Database & Lexicon Storage

- [x] 1.1 Create `LexiconTable` and `UserFlashcardTable` in `server/src/main/kotlin/ir/speaking/feature/` and register them in `Databases.kt` table initialization. Verify tables create cleanly without schema errors.
- [x] 1.2 Implement `LexiconRepository` and `UserFlashcardRepository` with CRUD operations for caching lemmas, checking review due cards, and updating box numbers. Verify repository unit tests pass.

## 2. 9router Dictionary Engine & Kokoro Audio Caching

- [x] 2.1 Implement `DictionaryService` integrating with `AiApiService` using a structured prompt for 9router to extract lemmas, phonetics, CEFR levels, and contextual Persian translations. Verify JSON decoding parses sample responses.
- [x] 2.2 Wire automatic audio synthesis in `DictionaryService` using `TtsService` (Kokoro), storing audio in `audio_cache/words/{lemma}.mp3` and serving static URLs. Verify audio file is saved on disk.
- [x] 2.3 Implement Redis caching layer in `DictionaryService` keyed by normalized query strings to serve repeated lookups in <50ms. Verify cache hit returns before AI service call.

## 3. Backend Leitner Engine & API Endpoints

- [x] 3.1 Implement `LeitnerService` managing 5-box intervals (1, 3, 7, 14, 30 days) and 1-step back penalties upon review failures. Verify unit tests for box promotion and demotion.
- [x] 3.2 Implement Ktor routing under `/api/dictionary/lookup`, `/api/leitner/cards`, `/api/leitner/review`, and `/api/leitner/reminder-time`. Verify endpoints with integration tests.

## 4. Client Data & Domain Layer

- [x] 4.1 Define domain models (`DictionaryEntry`, `Flashcard`, `LeitnerBox`) in `:domain`. Verify Gradle compilation across all targets.
- [x] 4.2 Implement `DictionaryRepository` and `LeitnerRepository` in `:data` with network DTOs and mappers. Verify responses map cleanly to domain models.
- [x] 4.3 Implement Use Cases: `LookupWordUseCase`, `AddCardToLeitnerUseCase`, `GetDueCardsUseCase`, and `SubmitCardReviewUseCase`. Verify use cases in test suite.

## 5. In-Chat Word Selection & Dictionary Sheet

- [x] 5.1 Refactor action chips row in `AiChatItem.kt` to accommodate `[🔊 پخش]` `[🌐 ترجمه]` `[📖 دیکشنری]` and compact `[📋]` copy icon. Verify layout on mobile width without wrapping.
- [x] 5.2 Implement `WordTokenSelectorSheet.kt` in `feature/chat` rendering message tokens as selectable chips with Serene Cyan highlight and confirmation preview. Verify interactive token selection in simulator.
- [x] 5.3 Implement `DictionaryDetailSheet.kt` displaying phonetic, part of speech, contextual translation, Kokoro audio playback, and "Add to Leitner" button. Verify sheet opens with correct data.

## 6. Leitner Screen & 3D Coverflow Review Carousel

- [x] 6.1 Create `feature/leitner` module or package with `LeitnerScreen.kt`, box filter tabs (`همه`, `آماده مرور`, `جعبه ۱..۵`), and a 2-column card grid with ambient bottom glows. Verify grid renders smoothly.
- [x] 6.2 Implement focused card inspection on grid tap: frosted glass backdrop with interactive 3D flip revealing details. Verify flip animation in UI.
- [x] 6.3 Implement `LeitnerReviewDialog.kt` full-screen modal featuring 3D Coverflow Pager (`rotationY`, `scale`, `alpha`) with "بلدم" and "بلد نیستم" action buttons. Verify coverflow physics and card advancement.

## 7. Journey Map Edge Tab & Daily Notification Reminders

- [x] 7.1 Add vertical rotated edge tab ("لایتنر") pinned to the right edge of `JourneyMapScreen.kt` with an unreviewed cards counter badge. Verify positioning does not overlap `StageCard`.
- [x] 7.2 Implement `LeitnerReminderSheet.kt` with a keyboardless numeric stepper (`+` and `-` buttons for hour and minute) and system push notification permission trigger. Verify reminder time updates in backend.
- [x] 7.3 Register `LeitnerRoute` in `navigation/Routes.kt` and wire navigation destinations in `AppNavigation.kt`. Verify deep linking and navigation back stacks.

## 8. Verification & Integration Testing

- [ ] 8.1 Execute end-to-end flow: open chat -> select word -> view dictionary sheet -> add to Leitner -> review card in 3D coverflow -> advance box.
- [ ] 8.2 Verify build compilation across JVM, Android, iOS, and Web (Wasm/JS) via `./gradlew build -x test`.
