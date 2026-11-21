# Proposal: Smart Contextual Dictionary & Spaced Repetition Leitner Box

## Why

Language learners frequently encounter unfamiliar words and idiomatic expressions during AI conversations, but without a structured vocabulary retention system, passive comprehension quickly fades. 
This change introduces a seamless in-chat word/phrase lookup powered by an LLM dictionary combo in 9router alongside a modern 5-box Leitner Spaced Repetition System. It empowers users to immediately capture contextual vocabulary from conversations, practice active recall through a tactile 3D coverflow review interface, and build long-term retention supported by daily notification reminders.

## What Changes

- **In-Chat Word Selection UX**:
  - Re-align chat bubble action chips in `AiChatItem` to include a prominent `[📖 دیکشنری]` chip alongside voice and translation, turning copy into a compact icon.
  - Interactive **Word-Token Selector Sheet**: Clicking dictionary opens a bottom sheet parsing the message into clickable token pills, allowing users to effortlessly select a single word or multi-word phrase without struggling with OS text-selection handles.
- **Context-Aware Dictionary Engine & Backend Cache**:
  - Introduce global lexicon cache in PostgreSQL (`lexicon_entries`) and Redis to serve previously queried lemmas and definitions in <50ms.
  - Connect cache misses to a dedicated **9router Dictionary Combo** returning structured JSON (headword lemma, phonetics, part of speech, contextual translation, general definitions, CEFR level, and bilingual examples).
  - Automatically synthesize and cache pronunciation audio via Kokoro TTS (`/audio_cache/words/{lemma}.mp3`) on first lookup.
- **5-Box Spaced Repetition Leitner System**:
  - Data model (`user_flashcards`) tracking cards across boxes 1 to 5 with review intervals (Day 1, 3, 7, 14, 30).
  - Friendly 1-step-back penalty upon review failure (e.g., Box 4 drops to Box 3, never harsh resets to Box 1).
  - Modern Grid view filtering by due status and boxes, featuring subtle ambient glow accents per card.
  - Interactive Card Focus on grid tap: frosted glass backdrop bringing the tapped card to center with 3D flip animation.
  - **Full-Screen 3D Coverflow Review Carousel**: High-polish review session adapting the proven 3D carousel mechanics (`rotationY`, `scale`, `alpha`) from `SubscriptionScreen`.
- **Product Placement & Daily Reminders**:
  - Vertical edge tab ("لایتنر") pinned to the middle-right border of `JourneyMapScreen` with an unreviewed counter badge.
  - Stepper Time-Picker bottom sheet in Leitner TopAppBar with large `+` / `-` buttons for hour and minute, triggering web/Android push notification permission requests and syncing daily cron reminder schedules.

## Capabilities

### New Capabilities
- `dictionary`: Contextual dictionary lookup, interactive word-token selection sheet, global lexicon cache, Kokoro TTS word audio caching, and 9router structured translation combo.
- `leitner-box`: Spaced repetition flashcard deck, 5-box review engine with 1-step penalty, grid and 3D coverflow review modes, edge entry tab on journey map, and daily push notification scheduling.

### Modified Capabilities
<!-- No existing OpenSpec capabilities to modify (initial OpenSpec setup) -->

## Impact

- **Client (`feature/chat`, `feature/main`, new `feature/leitner`)**:
  - Update `AiChatItem.kt` action chips row layout.
  - Implement `WordTokenSelectorSheet.kt` in `feature/chat`.
  - Add vertical edge tab in `JourneyMapScreen.kt`.
  - Create new module or screen package for Leitner with Grid, 3D Coverflow Pager, and Time Picker.
  - Register `LeitnerRoute` in `navigation/Routes.kt` and `AppNavigation.kt`.
- **Server (`server/`)**:
  - Add Exposed tables: `LexiconTable` and `UserFlashcardTable`.
  - Add `DictionaryService` with 9router prompt integration and Kokoro audio caching.
  - Add `LeitnerService` for fetching due cards, processing box upgrades/penalties, and setting reminder timestamps.
  - Register routes under `/api/dictionary/*` and `/api/leitner/*`.
  - Integrate reminder notification job into daily scheduler.
