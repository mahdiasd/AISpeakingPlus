# Spec Delta

## Purpose

Provides in-chat word and phrase lookup with contextual understanding, structured lemma definitions, audio pronunciation, and caching to support seamless vocabulary acquisition during conversation.

## ADDED Requirements

### Requirement: Word Token Selection Interface
The system SHALL provide an interactive word-token bottom sheet that splits message text into selectable word chips for single or multi-word phrase lookup.

#### Scenario: User opens word selector from AI chat bubble
- **WHEN** user taps the dictionary chip on an AI chat message
- **THEN** the system displays a bottom sheet containing all message words as distinct interactive chips

#### Scenario: User selects a single word
- **WHEN** user taps a specific word chip in the bottom sheet
- **THEN** the chip highlights in Serene Cyan and the selected word is previewed in the confirmation panel

#### Scenario: User selects a multi-word phrase
- **WHEN** user taps multiple contiguous word chips in the bottom sheet
- **THEN** the system aggregates them into an ordered phrase and updates the confirmation preview

### Requirement: Context-Aware Lexicon Caching
The system SHALL query local database and Redis lexicon cache before requesting external LLM translation for selected words.

#### Scenario: Lookup for previously cached word
- **WHEN** a user requests dictionary definition for a word present in the lexicon cache
- **THEN** the system returns cached lemma, phonetic, parts of speech, and translation within 100 milliseconds without calling the AI service

#### Scenario: Cache miss routes to 9router combo
- **WHEN** a user requests dictionary definition for a word not present in the lexicon cache
- **THEN** the system calls the 9router dictionary combo with the target word and sentence context, storing the structured result in the lexicon cache

### Requirement: Structured Dictionary Translation Output
The system SHALL deliver consistent structured definitions including base lemma, IPA phonetic notation, contextual Persian translation, alternative meanings, and bilingual example usage.

#### Scenario: Successful dictionary response rendering
- **WHEN** the backend returns a dictionary entry
- **THEN** the client sheet renders the base lemma, phonetic transcription, part of speech badge, contextual Persian translation, and an add-to-deck action button

#### Scenario: Fallback when context translation is unavailable
- **WHEN** contextual definition cannot be derived from sentence
- **THEN** the system presents general dictionary translations and logs a diagnostic warning without crashing

### Requirement: Word Pronunciation Audio Synthesis
The system SHALL generate and serve high-fidelity audio pronunciation for dictionary headwords using Kokoro TTS, caching generated audio files for instant subsequent playback.

#### Scenario: Audio playback on first lookup
- **WHEN** user taps the audio listen icon on a dictionary entry with no cached audio
- **THEN** the backend synthesizes pronunciation audio, caches it to disk, and streams the audio URL to the client

#### Scenario: Instant playback for cached audio
- **WHEN** user taps the audio listen icon on a dictionary entry with pre-existing cached audio
- **THEN** the client immediately plays the cached audio without re-triggering speech synthesis
