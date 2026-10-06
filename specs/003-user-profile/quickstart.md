# Quickstart & Verification Guide: User Profile & Account Overview

**Feature**: `003-user-profile`  
**Date**: 2026-10-06  

---

## 1. Prerequisites

1. PostgreSQL and Redis services running (or local docker-compose environment).
2. Ktor backend running on `http://localhost:8080`.
3. Valid authentication token generated via OTP login (`/api/v2/auth/otp/send` and `/api/v2/auth/otp/verify`).

---

## 2. Backend API Verification

### 2.1 Fetch Profile (`GET /api/v2/user/profile`)
```bash
curl -X GET "http://localhost:8080/api/v2/user/profile" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Accept: application/json"
```

**Expected Result**:
- Status: `200 OK`
- JSON response containing `phoneNumber`, `nickName`, `avatar`, `score`, `totalStars`, `completedStagesCount`, and nested `subscription` object with `remainingDays`.

### 2.2 Update Profile (`PUT /api/v2/user/profile`)
```bash
curl -X PUT "http://localhost:8080/api/v2/user/profile" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "nickName": "Reza NewName",
    "avatar": "avatar_g3"
  }'
```

**Expected Result**:
- Status: `200 OK`
- Returned JSON reflects updated `nickName` and `avatar`.

---

## 3. Client UI & Flow Verification Scenarios

### Scenario A: Navigation from Journey Map
1. Open application on Desktop/Android.
2. In the top bar of `JourneyMapScreen`, verify the Profile icon button is visible.
3. Tap the Profile icon.
4. **Verification**: Transition to `ProfileScreen` is smooth (< 1 second) and displays the user's name, avatar, and stats.

### Scenario B: Guest User Profile Experience
1. Clear application storage or open app as Guest without logging in.
2. Navigate to Profile screen.
3. **Verification**:
   - Displays "زبان‌آموز مهمان" without crashing.
   - Shows local stars earned from Stage 1.
   - Shows registration incentive banner ("ثبت‌نام و ذخیره پیشرفت").
   - Tapping the banner opens `LoginRoute`.

### Scenario C: Active Subscriber Display
1. Log in with an account having an active subscription.
2. Open Profile screen.
3. **Verification**:
   - Golden subscription badge ("اشتراک فعال") is shown.
   - Shows plan name and countdown of remaining days (e.g., "۴۵ روز باقیمانده").

### Scenario D: Profile Customization & Avatar Picker
1. On Profile screen, tap the edit icon next to nickname/avatar.
2. Select a different avatar from the avatar grid dialog and enter a new nickname.
3. Tap Save.
4. **Verification**:
   - Profile avatar and nickname update immediately.
   - Data persists upon navigating away and returning.

### Scenario E: Safe Sign-Out
1. On Profile screen, tap "خروج از حساب" (Sign Out).
2. **Verification**:
   - A confirmation dialog appears ("آیا مطمئن هستید که می‌خواهید خارج شوید؟").
   - Canceling dismisses the dialog without action.
   - Confirming clears auth token, resets tier, and navigates back to initial route.
