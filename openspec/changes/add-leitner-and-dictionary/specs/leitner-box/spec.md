# Spec Delta

## Purpose

Empowers learners with a 5-box spaced repetition system featuring tactile 3D coverflow review, flexible card penalties, contextual audio playback, and scheduled daily reminders.

## ADDED Requirements

### Requirement: Card Creation and Deck Storage
The system SHALL enable users to save words directly from dictionary lookups or via custom manual entry into their personal Leitner deck.

#### Scenario: Adding word from dictionary sheet
- **WHEN** user taps "Add to Leitner" in the dictionary sheet
- **THEN** the system creates a flashcard initialized in Box 1 with next review scheduled for the current day

#### Scenario: Manual custom card creation
- **WHEN** user submits a custom word with meaning through the manual addition dialog
- **THEN** the system creates a user flashcard in Box 1 and schedules it for immediate review

### Requirement: 5-Box Spaced Repetition Progression with Step-Down Penalty
The system SHALL advance cards across 5 intervals upon correct recall and decrement cards by exactly one box upon incorrect recall without hard resets to Box 1.

#### Scenario: Successful review progression
- **WHEN** user marks a card in Box 2 as remembered during review
- **THEN** the system advances the card to Box 3 and sets the next review date to 7 days from now

#### Scenario: Recall failure one-step penalty
- **WHEN** user marks a card in Box 4 as forgotten during review
- **THEN** the system demotes the card to Box 3 and sets the next review date to 3 days from now

#### Scenario: Recall failure in Box 1
- **WHEN** user marks a card in Box 1 as forgotten during review
- **THEN** the system retains the card in Box 1 and resets its review timestamp for the next review cycle

### Requirement: Leitner Grid and Focused Card Flip
The system SHALL present cards in a categorized grid view with ambient colored glows and allow focused 3D card inspection against a frosted blurred backdrop.

#### Scenario: Filtering cards by box tabs
- **WHEN** user selects a specific box tab (e.g., Box 2)
- **THEN** the grid displays only cards currently belonging to Box 2

#### Scenario: Tapping card in grid for focused preview
- **WHEN** user taps any card in the grid list
- **THEN** the system applies a blurred background scrim, centers the card, and enables tap-to-flip between English prompt and Persian definition

### Requirement: Full-Screen 3D Coverflow Review Session
The system SHALL provide an immersive full-screen review dialog utilizing 3D coverflow card physics with distinct "بلدم" and "بلد نیستم" action buttons.

#### Scenario: Launching review for due cards
- **WHEN** user taps the "شروع مرور امروز" button with due cards available
- **THEN** the system animates a full-screen sheet upward containing a 3D coverflow pager populated with today's due cards

#### Scenario: Flipping review card to reveal answer
- **WHEN** user taps a card in the review coverflow
- **THEN** the card executes a 3D Y-axis flip revealing Persian translation, pronunciation, example sentence, and an audio play button

#### Scenario: Completing the daily review session
- **WHEN** user reviews the final card in the queue
- **THEN** the system displays a celebratory completion screen summarizing cards promoted and demoted

### Requirement: Journey Map Edge Navigation Tab
The system SHALL display a subtle vertical edge pill labeled "لایتنر" pinned to the right screen border on the Journey Map with an unreviewed cards counter badge.

#### Scenario: Visual badge on unreviewed cards
- **WHEN** the user has cards due for review on the current day
- **THEN** the edge pill displays a circular Serene Cyan badge indicating the count of due cards

#### Scenario: Navigation from Journey Map
- **WHEN** user taps the vertical Leitner edge pill
- **THEN** the system navigates directly to the Leitner screen

### Requirement: Daily Review Reminder with Stepper Picker
The system SHALL allow users to configure daily review reminder notifications using a touch-friendly numeric stepper without opening a virtual keyboard.

#### Scenario: Setting notification time via stepper
- **WHEN** user adjusts hour and minute using large plus and minus stepper buttons and confirms
- **THEN** the system requests system notification permissions if not already granted and saves the daily reminder time
