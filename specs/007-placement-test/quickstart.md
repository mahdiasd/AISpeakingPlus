# Quickstart & Verification Guide: Placement Test (آزمون تعیین سطح)

**Feature**: `007-placement-test` | **Date**: 2026-11-13

## Prerequisites
1. Local Ktor server running or test suite configured.
2. WebApp or Android client running.
3. Test Account: Mobile `09152413498`, OTP `87799`.

---

## 1. Automated Verification Commands

### Backend Route Tests (Ktor TestApplication)
```bash
./gradlew :server:test --tests "ir.speaking.feature.placement.*"
```

### Shared UI / Multiplatform Multi-target Compile Check
```bash
./gradlew :domain:compileKotlinJvm :data:compileKotlinJvm :navigation:compileKotlinJvm :server:compileKotlin
```

---

## 2. End-to-End User Validation Flow

1. **Open Application** and log in or continue as guest.
2. **Navigate to Profile**: Tap the Profile icon in the top header.
3. **Verify Placement Card**:
   - Check that the new card «آزمون تعیین سطح انگلیسی» is visible.
   - Shows current level or "هنوز تعیین سطح نشده‌اید" with a call to action button.
4. **Enter Placement Test**:
   - Tap on the placement card.
   - App navigates to `PlacementTestScreen`.
   - Top bar displays centered title «آزمون تعیین سطح» and `AppBackButton` on top-right pointing right (`>`).
   - Progress bar displays "سوال ۱ از ۲۰".
5. **Answer Questions via Horizontal Pager**:
   - Question 1 prompt displayed in English with level badge (e.g. `A1`).
   - 4 options stacked beneath. Tapping an option selects it with instant tactile visual highlight.
   - Swipe horizontally to the left (or tap «سوال بعدی») to slide smoothly to Question 2.
   - Repeat through Question 20.
6. **Submit Test & View Results**:
   - On the 20th question, tap «ثبت و مشاهده نتیجه آزمون».
   - In < 500ms, the Result Screen loads with:
     - Prominent CEFR Level badge (e.g., `B1 - متوسط`).
     - Correct / Incorrect answer breakdown card (e.g. `۱۴ درست` / `۶ غلط`).
     - Descriptive educational feedback.
7. **Interactive Review of Questions**:
   - Tap «مرور سوالات و پاسخ‌ها».
   - Review list opens displaying all 20 questions with:
     - The user's chosen option.
     - Green checkmark for correct answers.
     - Red highlight for incorrect answers with the correct option highlighted in green.
8. **Profile Level Synchronization**:
   - Tap the back button to return to the Profile.
   - Verify the user's level badge is now updated to the newly earned level.
