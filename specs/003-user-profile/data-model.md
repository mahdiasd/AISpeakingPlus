# Phase 1 Data Model: User Profile & Account Overview

**Feature**: `003-user-profile`  
**Date**: 2026-10-06  
**Status**: Ready for Implementation  

---

## 1. Domain Entities (Client & Server)

### 1.1 `UserProfile` (Client Domain Model)
Represents a learner's account identity and profile details.

| Field | Type | Nullable | Description |
|---|---|---|---|
| `id` | `String` | No | Unique UUID of the user |
| `phoneNumber` | `String` | No | Iranian mobile number (e.g. "09121234567") |
| `nickName` | `String` | No | Display name / nickname (default: "Learner") |
| `firstName` | `String` | Yes | First name if provided |
| `lastName` | `String` | Yes | Last name if provided |
| `avatar` | `String` | No | Avatar identifier key (e.g. "avatar_g1", "default_avatar") |
| `gender` | `String` | Yes | User gender if provided |
| `score` | `Int` | No | Total learning XP / points |
| `totalStars` | `Int` | No | Cumulative stars earned across story stages |
| `completedStagesCount` | `Int` | No | Number of completed stages (stars >= 1) |
| `languageLevel` | `String` | No | CEFR level or level label (e.g. "A1", "B1") |
| `subscription` | `SubscriptionSummary` | No | Current subscription details |
| `isGuest` | `Boolean` | No | True if user is unauthenticated local guest |

### 1.2 `SubscriptionSummary` (Domain Model)
Represents the user's active entitlement and duration metrics.

| Field | Type | Nullable | Description |
|---|---|---|---|
| `isSubscriber` | `Boolean` | No | True if user has an active, non-expired subscription |
| `planType` | `String` | Yes | Identifier of current plan (e.g. "1_MONTH", "3_MONTHS", "6_MONTHS") |
| `planTitleFa` | `String` | Yes | Localized title (e.g. "اشتراک طلایی ۳ ماهه") |
| `startedAt` | `String` | Yes | ISO 8601 timestamp of subscription start |
| `expiresAt` | `String` | Yes | ISO 8601 timestamp of subscription expiration |
| `remainingDays` | `Int` | No | Calculated active days remaining (0 if expired or unsubscribed) |
| `isExpiringSoon` | `Boolean` | No | True if active and remainingDays <= 7 |

### 1.3 `UpdateProfileInput` (Domain Model)
Payload for updating user profile attributes.

| Field | Type | Constraints | Description |
|---|---|---|---|
| `nickName` | `String?` | 2..50 characters, trimmed | Updated display name |
| `avatar` | `String?` | Valid avatar identifier | Updated avatar key |
| `firstName` | `String?` | Max 50 characters | First name |
| `lastName` | `String?` | Max 50 characters | Last name |

---

## 2. Server Data & Persistence Schema

### 2.1 Existing Tables Involved

#### `UserTable` (`users`)
- `id`: UUID (Primary Key)
- `phone_number`: VARCHAR(20) Unique Index
- `nick_name`: VARCHAR(100) Default "Learner"
- `first_name`: VARCHAR(100) Nullable
- `last_name`: VARCHAR(100) Nullable
- `gender`: VARCHAR(16) Nullable
- `score`: INTEGER Default 0
- `avatar`: VARCHAR(255) Default "default_avatar"
- `status`: VARCHAR(32) Default "ACTIVE"
- `created_at`: TIMESTAMP
- `updated_at`: TIMESTAMP

#### `SubscriptionTable` (`subscriptions`)
- `id`: UUID (Primary Key)
- `user_id`: UUID Foreign Key -> `UserTable.id`
- `plan_type`: VARCHAR(32)
- `started_at`: TIMESTAMP
- `expires_at`: TIMESTAMP Index
- `status`: VARCHAR(32) ("ACTIVE", "EXPIRED", "REVOKED")

#### `StageProgressTable` (`stage_progress`)
- `id`: UUID (Primary Key)
- `user_id`: UUID Foreign Key -> `UserTable.id`
- `stage_id`: VARCHAR(64)
- `stars`: INTEGER (0..3)
- `score`: INTEGER
- `completed`: BOOLEAN

---

## 3. Data Transfer Objects (DTOs)

### 3.1 Network DTOs (`network` & `server`)

```kotlin
@Serializable
data class UserProfileResponseDto(
    val id: String,
    val phoneNumber: String,
    val nickName: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val avatar: String,
    val score: Int,
    val totalStars: Int,
    val completedStagesCount: Int,
    val languageLevel: String = "",
    val subscription: SubscriptionSummaryDto
)

@Serializable
data class SubscriptionSummaryDto(
    val isSubscriber: Boolean,
    val planType: String? = null,
    val planTitleFa: String? = null,
    val startedAt: String? = null,
    val expiresAt: String? = null,
    val remainingDays: Int = 0
)

@Serializable
data class UpdateProfileRequestDto(
    val nickName: String? = null,
    val avatar: String? = null,
    val firstName: String? = null,
    val lastName: String? = null
)
```

---

## 4. State Lifecycle & Transitions

```
                    +--------------------+
                    |    GUEST STATE     |
                    | (Local stars only) |
                    +---------+----------+
                              |
                     [Tap "Login/Register"
                      & Verify OTP via SMS]
                              |
                              v
                    +--------------------+
                    |  REGISTERED_FREE   |
                    | (Synced Stars,     |
                    |  Plan: Free)       |
                    +---------+----------+
                              |
                     [Purchase Premium
                      Plan via Gateway]
                              |
                              v
                    +--------------------+
                    |     SUBSCRIBER     |
                    | (Remaining Days > 0|
                    |  Golden Badge)     |
                    +---------+----------+
                              |
                     [Days reach 0 /
                      Subscription expires]
                              |
                              v
                    +--------------------+
                    |   EXPIRED PLAN     |
                    | (Remaining Days = 0|
                    |  Renew CTA active) |
                    +--------------------+
```
