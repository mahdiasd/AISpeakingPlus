# Decision: Story-Based Gamified Speaking Journey with Tiered Access & Evaluation Rubric

- **Slug**: story-based-speaking-journey
- **Decided**: 2026-09-18
- **Verdict**: go
- **Artifacts reviewed**: intake.md, research.md, problem.md, concept.md

## Scorecard

| Criterion | Rating | Justification |
|-----------|--------|---------------|
| Problem validity | strong | Language anxiety and rapid churn in unstructured AI chat are well-documented, pressing user issues. |
| Evidence strength | strong | Direct evidence from existing project specs (`APP_SPECIFICATION_V2.md`, `TECHNICAL_SPECIFICATION.md`) and industry benchmarks (Duolingo Max, ELSA). |
| Value vs. inaction | strong | Doing nothing leads to high churn and stalled conversions; progressive gating with story immersion drastically boosts FTUE and paid upgrades. |
| Feasibility / appetite | strong | Option B delivers high perceived value within a realistic medium appetite (3–4 weeks) using lightweight 2D illustrated visuals on CMP. |
| Strategic fit | strong | Perfectly aligned with the project constitution: Clean Architecture, Kotlin Multiplatform, Ktor Server, and high-performance API standards. |
| Risk posture | strong | Critical risks (graphics overhead, access gating security, LLM termination loops) are identified and mitigated via 2D assets and structured JSON responses. |

## Verdict & Rationale

**Verdict: GO.**

The concept has clear user demand, high strategic alignment with the project's architecture and monetization strategy, and an executable scope. Transitioning from open-ended chats to episodic 2D-illustrated narrative missions addresses the core problem of learner attrition. The 3-tiered access funnel (Guest Stage 1 -> OTP Sync Stage 2 -> Paywall Stage 3+) provides a proven, low-friction adoption path. Therefore, the assessment pipeline approves moving forward directly to Spec-Driven Development (`/speckit-specify`).

## If go — Handoff to `/speckit-specify`

- **Problem**: English learners suffer from conversational anxiety and drop out of unstructured AI chats; early mandatory registration and paywalls deter new learners before experiencing value.
- **Chosen approach**: Option B — Core Episodic Adventure with 2D Game Visuals & Tiered Funnel (full-screen intro cinematic, sequential stages with 2D illustrated backgrounds and game-styled controls, AI roleplay missions with objective termination, in-chat hint mechanism with star penalties, strict 0–3 star evaluation rubric, and 3-tiered access gating).
- **In scope / out of scope**:
  * *In scope*: Episodic intro cinematic; 2D illustrated background and game-style action buttons per stage; roleplay missions with structured termination (`is_mission_completed`); in-chat hint system; 0–3 star rubric; 3-tiered access gating (Guest -> OTP sync -> Paywall); session replay with high score preservation; leaderboard score calculation.
  * *Out of scope*: Heavy 3D rendering engines; non-linear branching narrative trees; avatar clothing/customization shops; syllable-level phoneme acoustics; offline AI roleplay conversation.
- **Success metrics**: >70% FTUE Stage 1 completion, >50% Guest-to-Registered OTP conversion at Stage 2, >12% Paid subscription conversion at Stage 3, >35% D7 retention.
- **Carried-forward open questions**:
  1. Asset delivery strategy for 2D stage backgrounds (bundled inside app vs. dynamically downloaded/cached from CDN).
  2. Conflict resolution policy when syncing local Guest Stage 1 progress if the user logs in with an existing phone number that already has server records.
  3. Exact number of story stages included in the initial launch/MVP.
