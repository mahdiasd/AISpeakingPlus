# Implementation Research: Story-Based Gamified Speaking Journey

**Feature Branch**: `001-story-based-speaking-journey`  
**Date**: 2026-09-18  
**Status**: Completed  

---

## 1. Guest Mode Persistence & Cloud Synchronization Strategy

### Context & Challenge
Users need to play Stage 1 with zero registration friction in Guest Mode. Their earned star rating (0–3), completion state, and best score must persist on device across app restarts. When they reach Stage 2 and complete SMS OTP verification, this local guest progress must be merged into their newly created or existing cloud account without data loss or regressions.

### Decision
- **Local Guest Storage**: Use multiplatform key-value persistence (`DataStore` / `MultiplatformSettings`) to store an unauthenticated progress payload: `Map<stageId, LocalStageProgress>`.
- **Sync Protocol**: Upon successful OTP verification on client (`/api/v1/user/auth/otp/verify`), the client immediately calls `POST /api/v2/progress/sync` providing the serialized local progress array:
  ```json
  {
    "items": [
      {
        "stageId": "stage-001",
        "stars": 3,
        "score": 95,
        "completedAt": "2026-09-18T12:00:00Z"
      }
    ]
  }
  ```
- **Conflict Resolution Rule**: The server processes the items inside a database transaction:
  $$\text{persisted\_stars} = \max(\text{local\_stars}, \text{cloud\_stars})$$
  $$\text{persisted\_score} = \max(\text{local\_score}, \text{cloud\_score})$$
- Once the sync endpoint responds with HTTP 200 and the updated progress list, the client clears the guest-specific flag and associates subsequent queries with the authenticated JWT session.

### Rationale
- Zero server database overhead for anonymous users who abandon the app before registering.
- Deterministic conflict resolution prevents high scores from ever being downgraded when a returning user logs in on a new device.

### Alternatives Considered
- **Anonymous Server Accounts**: Generating a temporary server guest account with an anonymous JWT.  
  *Rejected*: Bloats PostgreSQL with abandoned guest accounts and requires complex token reassignment logic upon registration.
- **Client-Side Overwrite**: Simply writing local state directly to the server without conflict check.  
  *Rejected*: If a returning user already achieved 3 stars on Stage 1 in the cloud previously, and achieves 1 star as a guest now, a blind overwrite would degrade their cloud score.

---

## 2. Star Evaluation Rubric & AI In-Chat Hint Engine

### Context & Challenge
The pedagogical value relies on rewarding fluency and accuracy while giving users an escape hatch when they don't know what to say. Every hint and grammar error must deduct stars objectively according to:
- 3 Stars: 0 errors and 0 hints
- 2 Stars: 1 total penalty (error or hint)
- 1 Star: 2 total penalties (errors + hints)
- 0 Stars: 3+ total penalties OR mission objective not satisfied

### Decision
- **In-Chat Hint Architecture**:
  - Client sends `POST /api/v2/stages/{stageId}/hint` with the last 3-4 dialogue messages.
  - Server invokes the LLM gateway with a lightweight fast prompt to generate:
    1. A natural English sentence recommendation or conversational starter.
    2. A Persian translation/guidance note.
  - The client increments its local `sessionHintsCount` and displays the suggestion in a gentle bottom cue.
- **Session Evaluation Architecture**:
  - When the dialogue ends (user completes the mission objective or reaches `maxTurns`), client calls `POST /api/v2/stages/{stageId}/evaluate` with the full session dialogue log and `hintsUsedCount`.
  - The server LLM gateway analyzes the dialogue against the stage's target objective using a structured JSON schema:
    ```json
    {
      "objectiveCompleted": true,
      "grammarErrorsCount": 1,
      "grammarErrors": [
        {
          "original": "I wants two ticket please",
          "correction": "I want two tickets please",
          "explanationFa": "برای فاعل I فعل بدون s سوم‌شخص می‌آید و بعد از two اسم باید جمع (tickets) باشد."
        }
      ],
      "feedbackFa": "مکالمه بسیار روانی داشتی! فقط به تطابق فاعل و فعل و جمع بستن اسامی دقت کن."
    }
    ```
  - Server calculates final stars:
    $$\text{totalPenalty} = \text{grammarErrorsCount} + \text{hintsUsedCount}$$
    $$\text{stars} = \begin{cases} 0 & \text{if } \neg\text{objectiveCompleted} \lor \text{totalPenalty} \ge 3 \\ 3 & \text{if } \text{totalPenalty} = 0 \\ 2 & \text{if } \text{totalPenalty} = 1 \\ 1 & \text{if } \text{totalPenalty} = 2 \end{cases}$$
  - The server updates `StageProgressTable` using the high-score preservation logic and returns the evaluation report.

### Rationale
- Offloading detailed grammar analysis to the end of the session guarantees sub-2.5s response times during active speech dialogue turns.
- Transparent mathematical rubric ensures learners understand exactly why they earned their stars.

### Alternatives Considered
- **Turn-by-Turn Grammar Interruption**: Checking grammar on every user turn and displaying warnings immediately.  
  *Rejected*: Disrupts conversational immersion and increases turn latency by 1-2 seconds.
- **Client-Side Regex Grammar Engine**: Performing grammar checks on the device.  
  *Rejected*: Incapable of contextual spoken English nuances, colloquialisms, and conversational intent.

---

## 3. 3-Tier Access Gating Architecture (Guest -> OTP -> Paywall)

### Context & Challenge
The system needs to balance user acquisition with business monetization through a frictionless funnel:
- **Tier 1 (Stage 1)**: Guest mode accessible without login.
- **Tier 2 (Stage 2)**: Free account registration via Mobile SMS OTP required.
- **Tier 3 (Stage 3+)**: Paid active subscription required.

### Decision
- **Server Enforcement**:
  - `GET /api/v2/stages`: Returns full catalog. For guest requests (missing Bearer token), stages 2+ have `locked = true`, with `lockReason = "REGISTRATION_REQUIRED"` for stage 2 and `"SUBSCRIPTION_REQUIRED"` for stages 3+.
  - `GET /api/v2/stages/{id}` & `POST /api/v2/stages/{id}/evaluate`:
    - Stage 1: Public endpoint, unauthenticated allowed.
    - Stage 2: Wrapped in `authenticate(USER_JWT_NAME)`. If missing or invalid, throws `AppException.UnauthorizedAccess()` (HTTP 401).
    - Stage 3+: Wrapped in `authenticate(USER_JWT_NAME)` plus a subscription gate interceptor `requireSubscription()`. If the user does not possess an active subscription in `SubscriptionTable` (or Redis cache), throws `AppException.PaymentRequired()` (HTTP 402).
- **Client Enforcement**:
  - Clicking Stage 2 when unauthenticated triggers the `RegisterBottomSheet` (Mobile number input -> SMS OTP verify -> Auto-sync -> Enter stage).
  - Clicking Stage 3+ without active subscription triggers the `PaywallDialog` / `PurchasesScreen` showing 1, 3, and 6-month plans, pricing in Tomans, and promo code entry.

### Rationale
- Defense-in-depth: Server rejects unauthorized access even if the client app is decompiled or modified.
- Smooth UX: Client provides informative context-aware modals rather than generic errors.

### Alternatives Considered
- **Client-Only Gating**: Enforcing locks in UI only and leaving endpoints open.  
  *Rejected*: Critical security flaw; users could script API calls to access all premium scenarios for free.
- **Paywall at Stage 1**: Requiring registration or payment upfront.  
  *Rejected*: Kills conversion; Stage 1 guest immersion is fundamental to user adoption.

---

## 4. Remote 2D Asset Delivery & Multiplatform Image Caching

### Context & Challenge
Stage immersion requires rich, full-screen 2D game backgrounds with thematic atmospheric visuals for all 15+ stages without inflating app bundle sizes. Images must render within 150ms on revisit.

### Decision
- **Asset Storage & Delivery**:
  - Images are compressed in modern WebP format (lossless/near-lossless, ~150-250KB each).
  - Served via Ktor static routing `/resources/stages/bg_{orderIndex}.webp` with HTTP caching headers (`Cache-Control: public, max-age=2592000, immutable`).
- **Client Loading & Cache (Coil 3)**:
  - Compose Multiplatform integrates Coil 3 (`coil-compose`, `coil-network-ktor3`).
  - Disk and memory caches are configured in `sharedUI` with a 100MB disk quota.
  - Backgrounds are rendered using `AsyncImage(model = stage.backgroundUrl)` with crossfade animation and frosted glass UI overlays for dialogue bubbles and buttons.

### Rationale
- Keeps initial app binary size lightweight (<25MB) while enabling crisp, retina-grade illustrations.
- HTTP cache headers + Coil 3 disk cache guarantees instantaneous (<150ms) rendering on repeat visits.

---

## 5. Narrative Catalog (15+ Linear Stages) & Leaderboard Aggregation

### Context & Challenge
The feature requires a cohesive narrative covering the protagonist's journey from Tehran to London across 15+ stages, progressing organically in vocabulary, grammar complexity, and real-life scenarios.

### Decision
- **15 Episodic Story Stages**:
  1. *Stage 1 (Guest)*: **Tehran Airport Departure** — Checking in bags, choosing a window seat, receiving boarding pass.
  2. *Stage 2 (OTP Gate)*: **In-Flight to London** — Ordering dinner on board, filling out immigration declaration card.
  3. *Stage 3 (Paywall)*: **Heathrow Border Control** — Answering immigration officer's questions on stay purpose and address.
  4. *Stage 4*: **Underground Station** — Asking station staff how to buy an Oyster card and navigate Piccadilly Line.
  5. *Stage 5*: **Bloomsbury Grocer** — Introducing oneself and retrieving flat keys left with the shopkeeper.
  6. *Stage 6*: **Airbnb Flat Check-in** — Meeting the host, inquiring about Wi-Fi, central heating, and waste recycling.
  7. *Stage 7*: **British Cafe Breakfast** — Ordering a traditional Full English breakfast and specifying tea with oat milk.
  8. *Stage 8*: **Mobile Network Store** — Inquiring about SIM card plans, monthly data allowances, and topping up.
  9. *Stage 9*: **High Street Bank** — Booking an appointment and inquiring about documents needed for an international account.
  10. *Stage 10*: **Boots Pharmacy** — Explaining seasonal flu symptoms, allergies, and buying over-the-counter medicine.
  11. *Stage 11*: **University Campus Tour** — Asking campus ambassador for library card activation and student union society sign-ups.
  12. *Stage 12*: **Traditional London Pub** — Ordering pints and bar snacks, engaging in casual small talk with locals about football.
  13. *Stage 13*: **Flatmate House Interview** — Discussing shared chore schedules, quiet hours, and bills with prospective flatmates.
  14. *Stage 14*: **Part-time Job Interview** — Answering situational and behavioral interview questions for a cafe barista position.
  15. *Stage 15*: **Metropolitan Police Desk** — Reporting a lost backpack on the bus, providing detailed item descriptions and contact info.

- **Dynamic Leaderboard Computation**:
  - Server aggregates user rankings by:
    $$\text{Score} = (\text{Total Stars} \times 1000) + (\text{Completed Stages} \times 100) + \text{Aggregate Points}$$
  - Top 100 users cached in Redis sorted sets (`ZADD leaderboard:journey`) for $O(\log N)$ rank queries and sub-10ms response times.
