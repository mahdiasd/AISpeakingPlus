# Concept: Story-Based Gamified Speaking Journey with Tiered Access & Evaluation Rubric

- **Slug**: story-based-speaking-journey
- **Created**: 2026-09-18
- **Recommended option**: Option B — Core Episodic Adventure with 2D Game Visuals & Tiered Funnel

## Options

### Option A — Minimal Linear Quests (Smallest Viable Increment)
- **Sketch**: Implement the 3-tiered access gate (Stage 1 guest, Stage 2 mobile OTP, Stage 3 paywall) and the strict 0–3 star evaluation rubric using existing standard chat UI views, adding only a static narrative prompt banner and basic star badges. Stages unlock sequentially, but lack customized 2D scene backgrounds, game-like controls, and intro cinematic video.
- **Appetite**: small (1–2 weeks)
- **Trade-offs**: Fast to deliver with minimal design overhead and zero asset weight; however, it fails to deliver the promised "game-like Disneyland/gamified adventure" feeling, risking lower emotional investment and diminished Stage 2/Stage 3 conversion rates.
- **Rabbit holes**: Users treating it as standard chat and losing motivation quickly; retrofitting game aesthetics later requiring UI refactoring.

### Option B — Core Episodic Adventure with 2D Game Visuals & Tiered Funnel (Balanced & Recommended)
- **Sketch**: Deliver the complete journey experience: full-screen episodic intro cinematic (skippable and replayable), sequential stage progression with distinct 2D game-style illustrated scene backgrounds and themed action buttons, interactive AI roleplay missions with objective termination, an in-chat hint mechanism that deducts stars, the strict 0–3 star rubric, and the 3-tiered access gating model (Guest -> OTP sync -> Paywall).
- **Appetite**: medium (3–4 weeks)
- **Trade-offs**: Fully satisfies all user requirements, visual styling desires, and monetization goals; requires disciplined asset pipeline coordination (illustrations and audio profiles) and careful handling of guest-to-cloud synchronization.
- **Rabbit holes**: Overly complex client asset bundling/caching for stage backgrounds; over-engineering AI mission completion prompts leading to edge-case loop locks.

### Option C — Full RPG Metagame with Branching Narratives & Dynamic Audio-Visual Engine
- **Sketch**: Everything in Option B plus non-linear narrative branches (choices determine subsequent levels), inventory systems, customizable character avatars with unlockable outfits, and live animated 2D skeletal rigs (e.g. Spine/DragonBones) reacting dynamically to speech tone.
- **Appetite**: large (2–3 months)
- **Trade-offs**: Maximum possible immersion and viral gamification; high risk of scope explosion, severe asset production bottlenecks, heavy client complexity across 4 KMP targets, and delayed market launch.
- **Rabbit holes**: Complex state machines for branching storylines; multiplatform compatibility issues with skeletal animation runtimes on Compose Web and Desktop; excessive asset production cycles.

## Recommendation

**Option B (Core Episodic Adventure with 2D Game Visuals & Tiered Funnel)** is strongly recommended. 

It directly solves the user retention crisis by providing a tangible, visually captivating game aesthetic (2D illustrations, video cinematic, game-styled buttons) and clear narrative objectives, while remaining tightly bound within a realistic medium appetite. Furthermore, it directly operationalizes the verified 3-tiered access gating funnel and the non-negotiable 0–3 star evaluation rubric without getting trapped in the heavy RPG complexity of Option C.

## Out of Scope (for the recommended option)

- Non-linear narrative branching or multi-path storyline trees (progression is strictly sequential).
- Real-time 3D polygon meshes or heavy 3D rendering engines.
- Avatar character customization, cosmetic shop, or inventory mechanics.
- Syllable-level phoneme waveform visualizers or deep acoustic pronunciation scoring.
- Full offline AI conversation capabilities.

## Assumptions to Validate

- 2D illustrated backgrounds can be effectively packaged/cached on client platforms (Android, Desktop, Web, iOS) under 100MB total initial footprint.
- The LLM structured JSON response can reliably determine `is_mission_completed` and grammar error counts within acceptable latency (<2.5 seconds total round-trip).
- When a guest user logs in via OTP at Stage 2, cloud synchronization can cleanly merge local Stage 1 high-score results without data loss or race conditions.
- The 0–3 star rubric provides fair and intuitive grading without punishing natural colloquial conversational English.
