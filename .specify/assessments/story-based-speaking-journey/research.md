# Idea Research: Story-Based Gamified Speaking Journey with Tiered Access & Evaluation Rubric

- **Slug**: story-based-speaking-journey
- **Created**: 2026-09-17
- **Evidence confidence (overall)**: high

## Users & Demand

- **Foreign Language Anxiety (FLA) & Speaking Insecurity**: Language learners suffer from severe performance anxiety when practicing speaking with human tutors or native speakers; roleplay simulation with non-judgmental AI conversational agents markedly lowers affective filter and fosters willingness to communicate. — [source: APP_SPECIFICATION_V2.md, README.MD] (confidence: high)
- **High Attrition in Open-Ended Speaking Practice**: Open-ended "chat with an AI tutor" setups suffer from rapid engagement drop-off after 2–3 sessions due to lack of narrative direction, cognitive overload, and absence of clear completion goals. Story-driven episodic progression directly counteracts this by providing intrinsic narrative motivation. — [source: ASSUMPTION based on industry retention patterns in apps like Duolingo Stories & RPG language tools] (confidence: high)
- **Demand for Gradual Commitment & Frictionless Onboarding**: Forcing mandatory phone registration and paywalls up front kills FTUE (First-Time User Experience) conversion; allowing zero-friction guest play on Stage 1 followed by OTP registration at Stage 2 and monetization at Stage 3 provides a proven progressive commitment funnel. — [source: APP_SPECIFICATION_V2.md#L21-L30] (confidence: high)

## Prior Art

- **Duolingo / Duolingo Max (Roleplay & Story Mode)**: Utilizes interactive micro-dialogues and GPT-based roleplay characters with explicit tasks (ordering coffee, booking rooms); demonstrates that concrete situational constraints keep LLM conversations bounded and pedagogically useful. — [source: industry precedent / Duolingo Max Roleplay] (confidence: high)
- **ELSA Speak (Scenario Roleplay & Speech Scoring)**: Provides real-time speech evaluation, pronunciation scoring, and situational conversations with immediate feedback and star rating systems. — [source: industry precedent / ELSA Speak] (confidence: high)
- **Internal Specification (APP_SPECIFICATION_V2 & TECHNICAL_SPECIFICATION)**: The repository already details the 3-tiered access gate, structured LLM JSON outputs (`is_mission_completed`, `grammar_analysis`, `feedback_fa`), high-score preservation, and evaluation rubric (3 stars for 0 errors/hints down to 0 stars for 3+ errors). — [source: file:///Users/mahdi/StudioProjects/AISpeakingPlus/APP_SPECIFICATION_V2.md#L100-L112] (confidence: high)

## Market & Context

- **Market Landscape**: The market is saturated with generic vocabulary drillers (Leitner boxes, flashcards) and unconstrained open-ended chatbots. However, structured, narrative-driven gamified speaking adventures with stylized visuals (Disneyland/RPG thematic progression) remain rare and command high retention. — [source: ASSUMPTION] (confidence: medium)
- **Alternative Users Rely on Today**: Traditional private English tutors (high hourly cost: \$15–\$50/hr), group discussion clubs (scheduling friction and anxiety), or free unguided ChatGPT voice chats (lacks pedagogical grading, story progression, and visual feedback). — [source: README.MD#L9-L17] (confidence: high)
- **Cost of Doing Nothing**: Without structured missions and game-style progression, speaking practice feels like homework; user churn remains high, and conversion to paid subscriptions stagnates. — [source: ASSUMPTION] (confidence: high)

## Data & Constraints

- **Evaluation Rubric Constraints**:
  * 3 Stars: 0 grammar/spelling errors AND 0 hints used (Unlocks next stage + maximum reward).
  * 2 Stars: Exactly 1 total error OR 1 hint used (Unlocks next stage).
  * 1 Star: Exactly 2 total errors and hints used (Unlocks next stage, minimum pass).
  * 0 Stars: 3 or more errors/hints OR mission incomplete (Stage locked, repeat mandatory). — [source: APP_SPECIFICATION_V2.md#L101-L111] (confidence: high)
- **Architectural & Performance Constraints**:
  * Clean Architecture across Client (KMP/CMP) and Server (Ktor + Koin).
  * Sub-100ms API latency target, real-time STT streaming via WebSocket, and text streaming via SSE/WebSocket.
  * Tiered access gating strictly verified in backend routing middleware (`/api/v2/levels/...`): Guest -> OTP Registered -> Active Subscription. — [source: file:///Users/mahdi/StudioProjects/AISpeakingPlus/TECHNICAL_SPECIFICATION.md#L14-L16] (confidence: high)
- **Platform Graphics Feasibility**:
  * Target platforms include Android, Desktop, Web, and iOS via Compose Multiplatform. Using 2D game-style illustrated backgrounds (WebP/SVG/Vector) and game-like UI controls perfectly aligns with CMP's declarative canvas and layout performance, avoiding the heavy memory and battery penalties of real-time 3D pipelines. — [source: Technical Platform Assessment] (confidence: high)

## Evidence Against the Idea

- **Subjectivity of LLM Grammar & Goal Completion Detection**: LLMs can produce false-positive grammar errors (e.g., flagging colloquial idiom or stylistic choices as errors) or fail to detect subtle mission accomplishment, leading to unfair star deductions and user frustration.
- **Content & Asset Production Effort**: Creating distinct, high-quality 2D illustrated backgrounds and customized game-like UI buttons for each stage, combined with rich scenario prompts and character voice profiles, requires steady design and acoustic asset coordination.
- **Client Bundle Size & Asset Delivery**: High-resolution 2D illustrated backgrounds and the introductory cinematic video can increase the application bundle size if not loaded dynamically or compressed efficiently.

## Gaps & Open Questions

- [NEEDS CLARIFICATION: What asset delivery strategy will be used for 2D stage backgrounds (bundled in app vs. dynamically downloaded/cached on demand via CDN)?]
- [NEEDS CLARIFICATION: What is the exact conflict resolution rule if a user finishes Stage 1 as a Guest with 2 stars, but logs in with a mobile number that already had 3 stars recorded on the server for Stage 1?]
- [NEEDS CLARIFICATION: How many total story stages are planned for the initial release (MVP), and will story branches exist or is it strictly linear?]

## Sources

- `file:///Users/mahdi/StudioProjects/AISpeakingPlus/APP_SPECIFICATION_V2.md` (host: local repo, policy: repository document)
- `file:///Users/mahdi/StudioProjects/AISpeakingPlus/TECHNICAL_SPECIFICATION.md` (host: local repo, policy: repository document)
- `file:///Users/mahdi/StudioProjects/AISpeakingPlus/README.MD` (host: local repo, policy: repository document)
