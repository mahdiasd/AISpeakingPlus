# Contract: User Profile API Specifications

**Base URL**: `/api/v2/user`  
**Authentication**: Bearer JWT (`Authorization: Bearer <token>`) via `MyConstant.USER_JWT_NAME`

---

## 1. `GET /api/v2/user/profile`

Retrieve complete profile, aggregated achievements, and subscription status for the authenticated user.

### Headers
- `Authorization: Bearer <token>` (Required)

### Query Parameters
*None*

### Response: `200 OK`
```json
{
  "success": true,
  "message": "User profile retrieved successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "phoneNumber": "09121234567",
    "nickName": "Ali Reza",
    "firstName": "Ali",
    "lastName": "Rezaei",
    "avatar": "avatar_g1",
    "score": 1450,
    "totalStars": 24,
    "completedStagesCount": 8,
    "languageLevel": "B1",
    "subscription": {
      "isSubscriber": true,
      "planType": "3_MONTHS",
      "planTitleFa": "اشتراک ۳ ماهه طلایی",
      "startedAt": "2026-09-15T10:00:00Z",
      "expiresAt": "2026-12-15T10:00:00Z",
      "remainingDays": 70
    }
  }
}
```

### Error Responses
- **`401 Unauthorized`**: Token missing or invalid.
  ```json
  {
    "success": false,
    "message": "Authentication required",
    "data": null
  }
  ```
- **`404 Not Found`**: User not found in database.
  ```json
  {
    "success": false,
    "message": "User not found",
    "data": null
  }
  ```

---

## 2. `PUT /api/v2/user/profile`

Update user profile details (nickname, avatar, and optional first/last name).

### Headers
- `Authorization: Bearer <token>` (Required)
- `Content-Type: application/json`

### Request Body
```json
{
  "nickName": "Saman",
  "avatar": "avatar_g5",
  "firstName": "Saman",
  "lastName": "Karimi"
}
```

### Validation Rules
- `nickName`: Optional string. If provided, length must be between 2 and 50 characters, non-blank after trimming.
- `avatar`: Optional string. Must match recognized avatar token identifier.
- `firstName`: Optional string, max length 50.
- `lastName`: Optional string, max length 50.

### Response: `200 OK`
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "phoneNumber": "09121234567",
    "nickName": "Saman",
    "firstName": "Saman",
    "lastName": "Karimi",
    "avatar": "avatar_g5",
    "score": 1450,
    "totalStars": 24,
    "completedStagesCount": 8,
    "languageLevel": "B1",
    "subscription": {
      "isSubscriber": true,
      "planType": "3_MONTHS",
      "planTitleFa": "اشتراک ۳ ماهه طلایی",
      "startedAt": "2026-09-15T10:00:00Z",
      "expiresAt": "2026-12-15T10:00:00Z",
      "remainingDays": 70
    }
  }
}
```

### Error Responses
- **`400 Bad Request`**: Validation failed (e.g. nickname too short).
  ```json
  {
    "success": false,
    "message": "نام مستعار باید بین ۲ تا ۵۰ نویسه باشد",
    "data": null
  }
  ```
- **`401 Unauthorized`**: Authentication token missing or invalid.
