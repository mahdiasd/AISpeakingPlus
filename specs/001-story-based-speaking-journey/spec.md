# Feature Specification: Story-Based Gamified Speaking Journey with Tiered Access & Evaluation Rubric

**Feature Branch**: `001-story-based-speaking-journey`

**Created**: 2026-09-18

**Status**: Draft

**Input**: User description: "story-based-speaking-journey: episodic roleplay missions with 2D game aesthetics, 0-3 star evaluation rubric deducting for hints and grammar errors, and 3-tiered access gating (Guest -> OTP Registration -> Paywall)"

## Clarifications

### Session 2026-09-18

- Q: When a paid user's subscription expires, how should access to already-completed premium stages (Stage 3+) be handled? (FR-003) → A: Strict Paywall: Immediately lock all Stage 3+ content (including previously completed stages); active subscription is strictly required to play or replay any stage 3+.

### Session 2026-09-29 (Narrative Goals, AI Resistance, Bilingual Objectives & Vertical Assets)

- Q: How should stage objectives be framed? → A: Human Need & Motivation rather than Procedural Directives: Instead of mechanical commands (e.g. "Order chicken and ask about the landing card"), objectives must describe diegetic needs and conversational challenges (e.g. "Satisfy in-flight meal hunger and clarify UK landing card procedures prior to landing").
- Q: How should AI NPCs behave during multi-part objectives? → A: Anti-Spoon-feeding & Natural Friction: The AI NPC must NOT proactively remind or resolve the user's secondary objectives (to avoid trivial auto-wins). If the user neglects an objective dimension before dialogue turns exhaust, the evaluation fails (0 stars). NPCs exhibit role-authentic friction (e.g. grocer demands identity verification before surrendering keys; border officer probes vague answers).
- Q: How are objectives presented in the UI? → A: Bilingual Exposure: Every stage stores both English (`targetObjective`) and Persian (`targetObjectiveFa`) to be displayed in the stage briefing modal and chat header.
- Q: What is the format for background artwork? → A: Vertical Mobile-First (9:16 Portrait): Stage backgrounds must be vertical (aspect ratio 9:16, e.g. 1080x1920) designed for full-screen smartphones (replacing legacy horizontal assets).
- Q: How is NPC roleplay behavior configured? → A: `characterBehavior` field added to `stages` database table and web admin panel to define persona, skepticism, tone, and challenge parameters.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Frictionless Guest Entry & Stage 1 Immersion (Priority: P1)

A new language learner opens the app for the first time, watches or skips an animated story prologue introducing the main protagonist traveling from Tehran to London, lands on a 2D game-style illustrated stage screen, and undertakes their first spoken mission (e.g., retrieving an apartment key from a local grocer) as a Guest with zero registration friction.

**Why this priority**: First-Time User Experience (FTUE) is vital. Eliminating mandatory registration allows learners to instantly experience conversational value, drastically reducing drop-off before they commit.

**Independent Test**: Can be fully tested by launching the app as a fresh guest user, viewing/skipping the cinematic intro, playing through Stage 1 conversationally, and receiving an initial star evaluation saved locally.

**Acceptance Scenarios**:

1. **Given** a first-time user opening the app, **When** the app initializes, **Then** a full-screen story cinematic video plays with an option to skip, and the protagonist avatar settles into the top navigation header.
2. **Given** a user in Guest Mode on Stage 1, **When** they view the stage briefing, **Then** they see a stylized 2D illustrated background representing the scenario and a clear mission goal statement.
3. **Given** a user undertaking Stage 1, **When** the conversation starts, **Then** the dialogue proceeds according to the scenario configuration (either character or user initiates), with real-time speech-to-text input and natural voice responses.
4. **Given** a user completing the conversational objective in Stage 1, **When** the mission succeeds, **Then** the system calculates stars (0 to 3) according to grammar errors and hints used and stores the record in local storage.

---

### User Story 2 - Star Evaluation Rubric & In-Chat Hint Penalty (Priority: P1)

During any stage conversation, a learner receives an objective evaluation based on their spoken English accuracy and independence. Using in-chat hints is allowed to unblock the conversation, but each hint and grammar error reduces the maximum achievable stars.

**Why this priority**: Transparent, game-like grading provides immediate pedagogical feedback and intrinsic replay value, motivating users to achieve 3 stars while preventing them from being trapped if they don't know what to say.

**Independent Test**: Can be tested by running mock or live conversations with varied error counts and hint clicks, verifying that:
- 0 errors + 0 hints yields 3 stars
- 1 total error/hint yields 2 stars
- 2 total errors/hints yields 1 star
- 3+ errors/hints or failed mission yields 0 stars

**Acceptance Scenarios**:

1. **Given** an ongoing conversational mission, **When** the user is stuck and presses the "💡 Hint" button, **Then** the system presents a contextual sentence suggestion or cue, and increments the session hint penalty counter by 1.
2. **Given** a completed mission with 0 grammar errors and 0 hints used, **When** the mission ends, **Then** the user is awarded 3 stars (⭐⭐⭐) and unlocks the next stage.
3. **Given** a completed mission with exactly 1 grammar error and 0 hints (or 0 errors and 1 hint), **When** the mission ends, **Then** the user is awarded 2 stars (⭐⭐) and unlocks the next stage.
4. **Given** a completed mission with a total sum of 2 (errors + hints), **When** the mission ends, **Then** the user is awarded 1 star (⭐) and unlocks the next stage.
5. **Given** a mission with 3 or more total errors and hints, or where the objective was not satisfied, **When** the mission ends, **Then** the user receives 0 stars (⭕), the next stage remains locked, and repeat is required to advance.

---

### User Story 3 - Stage 2 Gate: Mobile OTP Registration & Local Progress Sync (Priority: P2)

When a guest user finishes Stage 1 and attempts to enter Stage 2, the app presents a registration modal requiring phone number verification via SMS OTP. Once verified, the local Stage 1 progress is securely synchronized to the user's new cloud profile.

**Why this priority**: Bridges the gap between free discovery and registered user retention, capturing user contact data for engagement and multi-device persistence without disrupting the learning momentum.

**Independent Test**: Can be tested by finishing Stage 1 as a guest, attempting to navigate to Stage 2, verifying that access is blocked until SMS OTP verification completes, and confirming Stage 1 stars appear on the server profile.

**Acceptance Scenarios**:

1. **Given** a guest user who has earned at least 1 star on Stage 1, **When** they tap to enter Stage 2, **Then** an OTP registration modal is displayed explaining that Stage 2 requires a free account.
2. **Given** the user enters a valid mobile number and correct SMS OTP, **When** verification succeeds, **Then** their account is created/authenticated, their local Stage 1 score is synced to their server profile, and Stage 2 opens.
3. **Given** an unregistered user attempting to bypass the client UI directly to access Stage 2, **When** the request reaches the server, **Then** the server rejects the request with an authentication required status.

---

### User Story 4 - Stage 3+ Paywall & Subscription Access Gating (Priority: P2)

When a registered free user completes Stage 2 and attempts to access Stage 3 or higher, the app presents a subscription paywall detailing membership plans, promo code input, and payment options.

**Why this priority**: Directly drives business monetization by gating intermediate and advanced narrative stages behind active premium subscriptions.

**Independent Test**: Can be tested with a registered user account without an active subscription attempting to open Stage 3, confirming the paywall is displayed, and verifying that activating a subscription unlocks Stage 3.

**Acceptance Scenarios**:

1. **Given** a registered user without an active subscription, **When** they attempt to enter Stage 3, **Then** the subscription paywall is displayed showing tiered subscription plans (1, 3, 6 months) and promo code redemption.
2. **Given** a user successfully completing subscription payment, **When** they return to the stage journey map, **Then** Stage 3 is unlocked and accessible.
3. **Given** a registered user without an active subscription attempting to fetch Stage 3 conversational data via API, **When** the server processes the request, **Then** the server rejects the request with a payment required status.
4. **Given** a user whose paid subscription has expired, **When** they attempt to play or replay any stage 3 or higher (even if previously completed with stars), **Then** access is blocked by the paywall requiring subscription renewal.

---

### User Story 5 - Replay & High Score Preservation (Priority: P3)

A learner revisits a previously completed stage from the session history or journey map to practice speaking and improve their rating. The system always preserves their best historical star rating.

**Why this priority**: Encourages mastery, fluency, and continuous practice without fear of losing previous accomplishments.

**Independent Test**: Complete a stage with 3 stars, replay it and score 1 star, and verify the record remains 3 stars.

**Acceptance Scenarios**:

1. **Given** a user with 3 stars on Stage 1, **When** they replay Stage 1 and achieve 1 star, **Then** the saved rating remains 3 stars on their profile and leaderboard.
2. **Given** a user with 1 star on Stage 2, **When** they replay Stage 2 and achieve 3 stars, **Then** the saved rating is updated to 3 stars, and leaderboard points increase accordingly.

---

### Edge Cases

- **Speech Recognition Network Drop**: If the internet connection drops during audio streaming, the system halts recording immediately, alerts the user with a retry dialog, and retains conversational context without consuming a hint or penalizing stars.
- **Mission Goal Not Met within Dialogue Limit**: If a user exchanges 10+ turns without fulfilling the objective, the NPC politely signals that the mission failed and offers a replay prompt.
- **OTP Rate Limiting & Brute-Force Defense**: If an OTP is requested repeatedly within a 2-minute cooldown window, or if incorrect OTP attempts exceed 3 tries, requests are blocked with clear cooldown countdowns.
- **Local Progress Sync Conflict (Max Stars Preserved)**: When a guest logs in with a mobile number that already has server records, the server applies the high-score rule: $\max(\text{local\_stars}, \text{cloud\_stars})$ is retained, ensuring previously earned 3-star achievements are never degraded.
- **Remote Asset Fetch & Local Caching**: Stage backgrounds are fetched dynamically from the server on demand, rendered full-screen with overlaid navigation and themed controls, and persistently cached locally on the client to ensure instant rendering on subsequent visits.
- **Rich Journey Catalog (15+ Stages)**: The narrative catalog supports over 15 sequential story stages covering the hero's journey from arrival in London through diverse situational milestones.
- **Expired Subscription Content Locking**: If a user's subscription expires, all Stage 3+ content immediately reverts to locked state, preventing both progression and replay until subscription renewal, while preserving all historical earned stars and leaderboard rankings.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST provide an introductory narrative cinematic on first launch with a user-accessible skip action and persistent avatar placement in the top header.
- **FR-002**: The system MUST enforce sequential stage progression where Stage $N+1$ unlocks only when the user achieves at least 1 star (⭐) on Stage $N$.
- **FR-003**: The system MUST support a 3-tiered access gating model:
  - Stage 1: Completely accessible in Guest Mode with local device persistence.
  - Stage 2: Requires free user registration via mobile number and SMS OTP.
  - Stage 3+: Requires an active paid subscription; upon subscription expiration, all Stage 3+ content (including previously completed stages) is immediately locked from playing or replaying until renewal.
- **FR-004**: The system MUST calculate stage evaluation scores using the strict 0–3 star rubric:
  - 3 Stars: 0 grammar/spelling errors AND 0 hints used.
  - 2 Stars: Exactly 1 total error OR 1 hint used.
  - 1 Star: Exactly 2 total errors and hints used.
  - 0 Stars: 3 or more errors/hints OR mission objective unfulfilled.
- **FR-005**: The system MUST provide an on-demand in-chat hint capability that delivers contextual sentence prompts while incrementing the session hint penalty count.
- **FR-006**: The system MUST evaluate mission completion via structured AI output; the AI NPC MUST NOT proactively complete or prompt forgotten user objectives; if the learner neglects required goal dimensions before dialogue exhaustion, the stage evaluates as failed (0 stars).
- **FR-007**: The system MUST preserve the highest historical star rating and score when a stage is replayed.
- **FR-008**: The system MUST synchronize local Stage 1 guest progress to the authenticated user account upon successful Stage 2 OTP login, resolving conflicts by preserving the maximum stars.
- **FR-009**: The system MUST dynamically fetch full-screen vertical (portrait, aspect ratio 9:16 mobile-optimized) 2D stage background illustrations from the server, cache them locally on device, and render game-styled menus and buttons directly over the background image.
- **FR-010**: The server MUST calculate leaderboard rankings dynamically based on total completed stages and aggregate stars earned.
- **FR-011**: The system MUST support an extensive linear catalog of at least 15+ thematic story stages.
- **FR-012**: The system MUST provide bilingual stage objectives in both English (`targetObjective`) and Persian (`targetObjectiveFa`) displayed prominently in the briefing dialog and chat header.
- **FR-013**: The system MUST support and persist NPC persona configuration (`characterBehavior`) defining roleplay tone, skepticism, verification gates, and friction barriers.

### Key Entities *(include if feature involves data)*

- **Stage / Mission**: Represents an individual episodic scenario. Attributes: identifier, sequence index (1 to 15+), bilingual title, bilingual narrative briefing, bilingual target objective (`targetObjective` & `targetObjectiveFa`), NPC character behavior prompt (`characterBehavior`), remote vertical portrait background URL (9:16), NPC persona name and avatar, voice profile, conversational initiator (user vs. NPC), and objective completion criteria.
- **User Progress**: Represents a learner's mastery of a stage. Attributes: user identifier, stage identifier, highest stars earned (0–3), completion timestamp, best total score, and repeat count.
- **Session Evaluation**: Represents the real-time record of a single stage attempt. Attributes: session identifier, stage identifier, grammar error count, hints used count, mission completed flag, and textual feedback in Persian.
- **Access Tier**: Represents user entitlement status: Guest, Registered Free, or Premium Subscriber (with expiration date).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: First-time users can launch the app, view/skip the intro, and start speaking in Stage 1 in under 30 seconds.
- **SC-002**: Over 70% of first-time users who enter Stage 1 complete the scenario on their first session.
- **SC-003**: Over 50% of users who successfully complete Stage 1 register via mobile OTP to proceed to Stage 2.
- **SC-004**: Over 12% of registered users reaching Stage 3 convert to paid subscribers.
- **SC-005**: Real-time spoken dialogue responses (STT transcription + AI conversational response streaming) deliver initial feedback within 2.5 seconds under standard mobile broadband conditions.
- **SC-006**: Replaying a completed stage preserves the user's best score and star rating in 100% of recorded sessions.
- **SC-007**: Once downloaded from the server, full-screen stage background images load from client local cache in under 150ms on all subsequent visits.

## Assumptions

- Users have a device with a functional microphone and audio playback hardware (speakers/headphones).
- AI roleplay conversation requires an active internet connection; fully offline speech recognition and LLM inference are out of scope.
- SMS OTP gateway service is available and configured for Iranian mobile network operators.
- Stage background illustrations are hosted on the server/CDN and loaded/cached dynamically on the client using Coil or an equivalent multiplatform image loader.
- Clean Architecture principles established in the project constitution govern all client (KMP/CMP) and server (Ktor/Koin) implementations.
