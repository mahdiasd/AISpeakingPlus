# Problem Definition: Story-Based Gamified Speaking Journey with Tiered Access & Evaluation Rubric

- **Slug**: story-based-speaking-journey
- **Created**: 2026-09-17
- **Inputs used**: intake.md, research.md, user input

## Problem Statement

English language learners lack engaging, low-anxiety environments to practice real-world spoken conversations, while open-ended AI chatbots cause cognitive fatigue and rapid churn due to missing narrative purpose, lack of structured progression, and unclear performance feedback. Simultaneously, immediate registration barriers and abrupt paywalls deter new learners before they experience core conversational value.

## Affected Users & Stakeholders

- **Users (English Learners / Speaking Aspirants)**: Individuals who possess foundational English vocabulary/grammar but freeze when speaking due to performance anxiety, lack of conversational partners, or absence of realistic simulated scenarios.
- **Users (First-Time Learners / Guests)**: Prospective learners who want instant proof-of-value without committing personal phone numbers or payments upfront.
- **Stakeholders (Product Lead & Founders)**: Need strong user retention (D1/D7/D30), viral progression loops, and a smooth conversion funnel from free guest play into registered active subscribers.
- **Stakeholders (Pedagogical & Content Designers)**: Need a transparent, objective framework to measure learner spoken fluency, grammar accuracy, and mission success without penalizing colloquial speech unfairly.

## Goals

- Provide an immersive, narrative-driven episodic journey where each conversational session has an unambiguous objective and situational context.
- Maintain high learner motivation and completion rates through gamified progression, clear star ratings (0 to 3 stars), and instantaneous feedback.
- Minimize initial drop-off by offering a zero-friction, registration-free Stage 1 trial that seamlessly converts to registered account status upon Stage 2 entry.
- Maximize monetization conversion at Stage 3 by establishing clear perceived value and narrative investment during Stages 1 and 2.
- Preserve learner achievements accurately across devices with high-score retention and local-to-cloud profile synchronization.

## Non-Goals

- Open-ended, unstructured general-purpose AI chat or freeform tutoring without situational objectives.
- Full real-time 3D polygonal graphics engine or hardware-intensive 3D rendering pipelines (focus is on lightweight, optimized 2D game-style illustrated visuals).
- Comprehensive multi-accent phoneme pronunciation diagnostics at the syllable level (focus is on grammar, conversational goal achievement, and spoken fluency).
- Complete offline operation for AI conversation (live speech recognition and LLM roleplay require internet connectivity; only cached static assets and local progress exist offline).

## Success Metrics

- **FTUE Completion Rate (Stage 1 Completion)**: >70% of first-time users complete Stage 1 without abandoning the app (baseline: unknown / industry benchmark ~40-50%).
- **Guest-to-Registered Conversion Rate (Stage 1 to Stage 2)**: >50% of users who complete Stage 1 register via mobile OTP to start Stage 2 (baseline: industry benchmark ~25-35%).
- **Paid Conversion Rate (Stage 2 to Stage 3 Paywall)**: >12% of users completing Stage 2 purchase a subscription (baseline: freemium average ~3-5%).
- **Day-7 Retention Rate**: >35% of learners actively returning to practice or replay stages for higher star scores.
- **Mission Progression Rate**: >80% of engaged learners achieve at least 1 star on their first or second attempt on any given stage.

## Cost of Inaction

Without a structured narrative journey and clear progressive gating, learners will continue to drop out after 1–2 disjointed speaking sessions due to lack of purpose. User acquisition costs will yield poor long-term retention, and free users will bounce before encountering or appreciating the premium subscription value proposition.

## Open Questions

- [NEEDS CLARIFICATION: What is the exact conflict resolution rule if a user finishes Stage 1 as a Guest with 2 stars, but logs in with a mobile number that already had 3 stars recorded on the server for Stage 1?]
- [NEEDS CLARIFICATION: What asset delivery strategy will be used for 2D stage backgrounds (bundled in app vs. dynamically downloaded/cached on demand via CDN)?]
- [NEEDS CLARIFICATION: How many total story stages are planned for the initial release (MVP), and will story branches exist or is it strictly linear?]

