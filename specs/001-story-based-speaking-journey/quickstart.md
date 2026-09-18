# Quickstart & Verification Guide: Story-Based Speaking Journey

**Feature Branch**: `001-story-based-speaking-journey`  
**Date**: 2026-09-18  
**Status**: Draft  

This guide provides end-to-end instructions for validating the 5 core user stories of the Story-Based Speaking Journey feature across the backend (Ktor) and client (Compose Multiplatform).

---

## 1. Prerequisites & Environment Setup

### 1.1 Backend & Database
1. Ensure PostgreSQL is running on `localhost:5432` with database `ai_speaking`.
2. Ensure Redis is running on `localhost:6379`.
3. Start the Ktor backend:
   ```bash
   ./gradlew :server:run
   ```
4. Verify server health:
   ```bash
   curl -s http://localhost:8080/health
   # Expected: {"status":"UP"}
   ```

### 1.2 Client Target
Run the Compose Multiplatform desktop client (or connect an Android emulator):
```bash
./gradlew :desktopApp:run
```

---

## 2. Validation Scenarios

### Scenario 1: Guest Launch & Stage 1 Immersion (User Story 1)
**Goal**: Verify that a first-time guest user can open the app, view the journey screen, enter Stage 1, and complete a conversational mission with zero login friction.

1. **Clean Client State**: Clear local persistent app data or launch fresh.
2. **Launch App**: Observe the cinematic story intro (Tehran to London) with a working "Skip" button.
3. **Inspect Journey Map**:
   - Stage 1 ("Tehran Airport Departure") is active and clickable (`UNLOCKED`).
   - Stage 2 displays a registration badge (`LOCKED_REGISTRATION`).
   - Stages 3–15 display subscription lock badges (`LOCKED_SUBSCRIPTION`).
4. **Enter Stage 1**:
   - The full-screen 2D airport background illustration loads and is cached locally.
   - The briefing modal shows: "خروج از فرودگاه امام تهران: دریافت کارت پرواز و صندلی پنجره".
5. **Roleplay**:
   - Check-in Agent speaks: "Hello! Where are you flying today?"
   - Speak or send: "Hello, I am flying to London and I would like a window seat please."
   - Agent responds: "Certainly! Here is your boarding pass for seat 12A."
   - Speak or send: "Thank you very much, have a nice day!"
6. **Verification Outcome**:
   - Mission objective achieved!
   - 0 hints used, 0 grammar errors detected.
   - User is awarded **3 Stars (⭐⭐⭐)** and 100 points.
   - Stars are saved locally in `LocalGuestProgress`.

---

### Scenario 2: Star Evaluation Rubric & Hint Deduction (User Story 2)
**Goal**: Verify that hints and grammar errors deterministically deduct stars according to the rubric.

1. **Test Run A (1 Hint used, 0 Grammar errors)**:
   - Click "💡 Hint" during chat.
   - Receive contextual suggestion: *"I want to check in my luggage, please."*
   - Complete dialogue without errors.
   - **Expected Outcome**: Total penalty = 1 $\rightarrow$ **2 Stars (⭐⭐)** awarded.
2. **Test Run B (1 Hint used, 1 Grammar error)**:
   - Click hint (penalty +1).
   - Speak with error: *"I has two bags"* (penalty +1).
   - Complete dialogue.
   - **Expected Outcome**: Total penalty = 2 $\rightarrow$ **1 Star (⭐)** awarded.
3. **Test Run C (3+ Penalties OR Unfulfilled Objective)**:
   - Use 2 hints + make 2 errors (total penalty = 4), OR fail to complete the goal.
   - **Expected Outcome**: Total penalty $\ge 3$ $\rightarrow$ **0 Stars (⭕)** awarded; next stage remains locked.

---

### Scenario 3: Stage 2 Gate & OTP Cloud Sync (User Story 3)
**Goal**: Verify that Stage 2 blocks unauthenticated guests, requires mobile OTP registration, and safely merges local Stage 1 progress to the cloud.

1. **Tap Stage 2**:
   - User taps Stage 2 ("In-Flight to London").
   - App presents the registration modal: *"ورود به مرحله ۲ نیازمند ایجاد حساب کاربری رایگان است."*
2. **Request SMS OTP**:
   - Enter Iranian mobile number (e.g. `09120000000`).
   - Tap "ارسال کد تایید".
3. **Verify OTP**:
   - Enter OTP code (or sandbox debug code `87799`).
   - Client receives JWT and user profile.
4. **Inspect Cloud Sync**:
   - Client immediately dispatches `POST /api/v2/progress/sync` with local Stage 1 record (3 stars).
   - Verify server responds HTTP 200 with saved records.
5. **Inspect Database**:
   ```sql
   SELECT user_id, stage_id, stars, best_score FROM stage_progress;
   ```
   - Confirms user now has `stage-01-tehran-departure` with 3 stars in PostgreSQL.
6. **Enter Stage 2**:
   - Stage 2 is now unlocked and accessible.

---

### Scenario 4: Stage 3+ Paywall & Subscription Gate (User Story 4)
**Goal**: Verify that Stage 3+ requires an active subscription and redirects free users to the paywall.

1. **Complete Stage 2**:
   - Play through Stage 2 with $\ge 1$ star.
2. **Tap Stage 3**:
   - Tap Stage 3 ("Heathrow Border Control").
   - App presents the Subscription Paywall (`PurchasesScreen` / Paywall Dialog).
   - Displays 1, 3, and 6-month plans with prices in Tomans and feature list.
3. **Direct API Verification (Defense-in-depth)**:
   ```bash
   curl -H "Authorization: Bearer <FREE_USER_JWT>" \
        http://localhost:8080/api/v2/stages/stage-03-heathrow-border
   ```
   - **Expected Status**: `402 Payment Required`
   - **Expected Body**: `{"status":402,"code":"SUBSCRIPTION_REQUIRED","message":"..."}`
4. **Subscribe & Unlock**:
   - Activate subscription for user in DB or via checkout API.
   - Return to app: Stage 3 is now unlocked!

---

### Scenario 5: Replay & High-Score Preservation (User Story 5)
**Goal**: Verify that replaying a stage preserves the learner's best historical rating.

1. **Replay Stage 1 (Initial score: 3 Stars)**:
   - User replays Stage 1.
   - Intentionally clicks 2 hints and makes 1 grammar mistake (total penalties = 3 $\rightarrow$ 0 stars).
2. **Check Evaluation Card**:
   - Shows this session score: 0 stars.
   - Shows message: *"بهترین امتیاز قبلی شما (۳ ستاره) محفوظ ماند."*
3. **Inspect Progress**:
   - Re-check `/api/v2/stages`: Stage 1 retains `stars: 3`, `repeatCount: 2`.
   - Leaderboard position and total stars are not degraded.

---

## 3. Automated Test Commands

```bash
# 1. Run Domain layer use-case unit tests
./gradlew :domain:test

# 2. Run Ktor Server integration & routing tests
./gradlew :server:test

# 3. Verify complete project compilation
./gradlew :server:compileKotlin :desktopApp:compileKotlin
```
