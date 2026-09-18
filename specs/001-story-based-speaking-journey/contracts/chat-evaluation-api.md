# API Contract: Chat Hint & Stage Evaluation

**Version**: `2.0.0`  
**Base URL**: `/api/v2/stages/{stageId}`  

---

## 1. Request Contextual Hint

Generates an on-demand contextual sentence prompt or starter in English along with a Persian guide translation to assist a stuck learner.

- **Endpoint**: `POST /api/v2/stages/{stageId}/hint`
- **Authentication**:
  - Stage 1: Optional (Guest allowed).
  - Stage 2: `Authorization: Bearer <token>` required.
  - Stage 3+: `Authorization: Bearer <token>` + Active subscription required.

### Request Body
```json
{
  "messages": [
    {
      "role": "Model",
      "content": "Good morning! Welcome to London Heathrow. May I see your passport and landing card, please?"
    }
  ]
}
```

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Hint generated successfully",
  "data": {
    "suggestionEn": "Good morning officer. Here is my passport and my landing card.",
    "explanationFa": "صبح بخیر جناب افسر. بفرمایید، این پاسپورت و کارت ورود من است."
  }
}
```

---

## 2. Submit Stage Evaluation

Submits the complete dialogue transcript, validates whether the mission objective was satisfied, calculates grammar errors and hints penalty, and updates user progress according to the 0–3 star rubric.

- **Endpoint**: `POST /api/v2/stages/{stageId}/evaluate`
- **Authentication**:
  - Stage 1: Optional (if authenticated, saves to cloud; if unauthenticated guest, returns calculated evaluation without cloud write).
  - Stage 2+: `Authorization: Bearer <token>` required.

### Request Body
```json
{
  "hintsUsedCount": 1,
  "turnsCount": 6,
  "transcript": [
    {
      "role": "Model",
      "content": "Hello! Where are you flying today?"
    },
    {
      "role": "User",
      "content": "Hello, I flying to London and I wants a window seat."
    },
    {
      "role": "Model",
      "content": "Sure, let me check that for you. Here is your boarding pass for seat 14A. Have a safe flight!"
    },
    {
      "role": "User",
      "content": "Thank you very much, have a nice day!"
    }
  ]
}
```

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Evaluation completed",
  "data": {
    "stageId": "stage-01-tehran-departure",
    "objectiveCompleted": true,
    "grammarErrorsCount": 1,
    "hintsUsedCount": 1,
    "totalPenalties": 2,
    "starsEarned": 1,
    "score": 75,
    "isHighScore": true,
    "unlockedNextStage": true,
    "grammarErrors": [
      {
        "original": "I flying to London and I wants a window seat",
        "correction": "I am flying to London and I want a window seat",
        "explanationFa": "برای زمان حال استمراری نیاز به فعل کمکی am دارید (I am flying) و برای فاعل I فعل بدون s می‌آید (I want)."
      }
    ],
    "feedbackFa": "هدف ماموریت با موفقیت انجام شد! مکالمه خوبی بود، برای کسب ۳ ستاره تلاش کن بدون راهنما و با اصلاح خطاهای گرامری مرحله را تکرار کنی."
  }
}
```

### Rubric Calculation Logic
$$\text{totalPenalties} = \text{grammarErrorsCount} + \text{hintsUsedCount}$$
$$\text{starsEarned} = \begin{cases}
0 & \text{if } \neg\text{objectiveCompleted} \lor \text{totalPenalties} \ge 3 \\
3 & \text{if } \text{totalPenalties} = 0 \\
2 & \text{if } \text{totalPenalties} = 1 \\
1 & \text{if } \text{totalPenalties} = 2
\end{cases}$$
