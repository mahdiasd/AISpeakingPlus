# API Contract: Stages & Progress Sync

**Version**: `2.0.0`  
**Base URL**: `/api/v2/stages` & `/api/v2/progress`  

---

## 1. Get Stage Journey Catalog

Fetches all episodic story stages along with the current user's progress and lock status.

- **Endpoint**: `GET /api/v2/stages`
- **Authentication**: Optional (Guest allowed). If `Authorization: Bearer <token>` is present, user-specific progress is loaded from database. If missing, all stages except Stage 1 are marked with appropriate lock reasons.

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Stages retrieved successfully",
  "data": {
    "currentTier": "GUEST",
    "stages": [
      {
        "id": "stage-01-tehran-departure",
        "orderIndex": 1,
        "title": "Tehran Airport Departure",
        "titleFa": "خروج از فرودگاه امام تهران",
        "briefingFa": "شما در باجه پذیرش فرودگاه هستید و باید کارت پرواز بگیرید و صندلی کنار پنجره درخواست کنید.",
        "backgroundUrl": "/resources/stages/bg_1.webp",
        "characterName": "Check-in Agent Sarah",
        "characterAvatarUrl": "/resources/avatars/sarah.webp",
        "characterGender": "Woman",
        "initialSpeaker": "Model",
        "maxTurns": 10,
        "lockStatus": "UNLOCKED",
        "progress": {
          "stars": 3,
          "bestScore": 95,
          "repeatCount": 2,
          "completedAt": "2026-09-18T10:00:00Z"
        }
      },
      {
        "id": "stage-02-inflight-london",
        "orderIndex": 2,
        "title": "In-Flight to London",
        "titleFa": "پرواز به سمت لندن",
        "briefingFa": "در طول پرواز، شام سفارش دهید و کارت ورود به بریتانیا را با مهماندار پر کنید.",
        "backgroundUrl": "/resources/stages/bg_2.webp",
        "characterName": "Flight Attendant James",
        "characterAvatarUrl": "/resources/avatars/james.webp",
        "characterGender": "Man",
        "initialSpeaker": "Model",
        "maxTurns": 10,
        "lockStatus": "LOCKED_REGISTRATION",
        "progress": null
      },
      {
        "id": "stage-03-heathrow-border",
        "orderIndex": 3,
        "title": "Heathrow Border Control",
        "titleFa": "کنترل گذرنامه هیترو لندن",
        "briefingFa": "به سوالات افسر مهاجرت در مورد هدف سفر و محل اقامت پاسخ دهید.",
        "backgroundUrl": "/resources/stages/bg_3.webp",
        "characterName": "Officer Davies",
        "characterAvatarUrl": "/resources/avatars/davies.webp",
        "characterGender": "Man",
        "initialSpeaker": "Model",
        "maxTurns": 12,
        "lockStatus": "LOCKED_SUBSCRIPTION",
        "progress": null
      }
    ]
  }
}
```

---

## 2. Get Single Stage Details

Fetches the complete briefing, character metadata, and scenario configuration for an individual stage.

- **Endpoint**: `GET /api/v2/stages/{stageId}`
- **Authentication**:
  - Stage 1: None required.
  - Stage 2: `Authorization: Bearer <token>` required (HTTP 401 if missing).
  - Stage 3+: `Authorization: Bearer <token>` + Active subscription required (HTTP 402 if not subscribed).

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Stage details retrieved",
  "data": {
    "id": "stage-01-tehran-departure",
    "orderIndex": 1,
    "title": "Tehran Airport Departure",
    "titleFa": "خروج از فرودگاه امام تهران",
    "briefing": "You are at the check-in desk at Tehran Airport. You have a long journey ahead and prefer a window view to pass the time.",
    "briefingFa": "شما در باجه پذیرش فرودگاه بین‌المللی امام خمینی هستید. بار سنگینی دارید و پرواز طولانی در پیش است؛ ترجیح می‌دهید صندلی کنار پنجره داشته باشید.",
    "targetObjective": "Complete airport check-in, secure a window seat, and obtain your boarding pass",
    "targetObjectiveFa": "انجام فرآیند پذیرش فرودگاه، تحویل بار، انتخاب صندلی مناسب کنار پنجره و دریافت کارت پرواز",
    "characterBehavior": "Sarah is a polite but busy airline agent. She verifies luggage weight, asks about seat preference, but won't offer a window seat automatically unless the traveler explicitly requests or inquires.",
    "backgroundUrl": "/resources/stages/bg_1_vertical.webp",
    "characterName": "Check-in Agent Sarah",
    "characterAvatarUrl": "/resources/avatars/sarah.webp",
    "characterGender": "Woman",
    "voiceId": "af_sarah",
    "initialSpeaker": "Model",
    "maxTurns": 10,
    "lockStatus": "UNLOCKED",
    "progress": {
      "stars": 3,
      "bestScore": 95,
      "repeatCount": 2,
      "completedAt": "2026-09-18T10:00:00Z"
    }
  }
}
```

> **UI & Asset Note**:
> 1. **Bilingual Objectives**: Both `targetObjective` and `targetObjectiveFa` are rendered in the client briefing dialog and chat screen header so the learner clearly understands the motivation in both languages.
> 2. **Vertical Mobile Asset**: `backgroundUrl` MUST point to vertical (portrait, aspect ratio 9:16 / 1080x1920) illustrations designed for edge-to-edge mobile screens.

### Error Responses
- **401 Unauthorized**:
  ```json
  {
    "status": 401,
    "message": "ثبت‌نام برای ورود به مرحله ۲ الزامی است.",
    "code": "AUTH_REQUIRED"
  }
  ```
- **402 Payment Required**:
  ```json
  {
    "status": 402,
    "message": "برای دسترسی به مرحله ۳ به بعد، اشتراک ویژه تهیه کنید.",
    "code": "SUBSCRIPTION_REQUIRED"
  }
  ```

---

## 3. Sync Guest Progress

Synchronizes local guest progress accumulated on device to the authenticated user account upon OTP registration.

- **Endpoint**: `POST /api/v2/progress/sync`
- **Authentication**: `Authorization: Bearer <token>` required.

### Request Body
```json
{
  "items": [
    {
      "stageId": "stage-01-tehran-departure",
      "stars": 3,
      "bestScore": 92,
      "completedAt": "2026-09-18T11:20:00Z"
    }
  ]
}
```

### Response (200 OK)
```json
{
  "status": 200,
  "message": "پیشرفت با موفقیت همگام‌سازی شد.",
  "data": [
    {
      "stageId": "stage-01-tehran-departure",
      "stars": 3,
      "bestScore": 92,
      "repeatCount": 1,
      "completedAt": "2026-09-18T11:20:00Z",
      "updatedAt": "2026-09-18T11:25:00Z"
    }
  ]
}
```
