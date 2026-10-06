# API Contract: Journey Leaderboard

**Version**: `2.0.0`  
**Base URL**: `/api/v2/leaderboard`  

---

## 1. Get Journey Leaderboard

Returns the global or weekly ranked list of learners based on total earned stars and completed journey stages.

- **Endpoint**: `GET /api/v2/leaderboard/journey?page=1&pageSize=50`
- **Authentication**: Optional (Bearer token loads current user rank position).

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Leaderboard retrieved",
  "data": {
    "currentUserRank": {
      "rank": 4,
      "userId": "d7480a47-c0e6-42d7-9878-fc7da91dfc1b",
      "displayName": "علی رضایی",
      "avatarUrl": "avatar_01",
      "totalStars": 38,
      "completedStages": 13
    },
    "items": [
      {
        "rank": 1,
        "userId": "e812a145-12a1-4776-90ab-aa12bb34cc56",
        "displayName": "سارا محمدی",
        "avatarUrl": "avatar_04",
        "totalStars": 45,
        "completedStages": 15
      },
      {
        "rank": 2,
        "userId": "b321c789-98ff-45cd-8721-dd56ee78aa99",
        "displayName": "امیر حسینی",
        "avatarUrl": "avatar_02",
        "totalStars": 42,
        "completedStages": 14
      },
      {
        "rank": 3,
        "userId": "c567d890-34ee-4890-aacc-112233445566",
        "displayName": "نیلوفر کاظمی",
        "avatarUrl": "avatar_07",
        "totalStars": 40,
        "completedStages": 14
      }
    ],
    "page": 1,
    "pageSize": 50,
    "totalCount": 1420
  }
}
```
