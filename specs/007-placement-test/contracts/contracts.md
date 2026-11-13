# Contracts: Placement Test (آزمون تعیین سطح)

**Feature**: `007-placement-test` | **Date**: 2026-11-13

## 1. Navigation Route Contract

```kotlin
package ir.aispeaking.navigation

import kotlinx.serialization.Serializable

@Serializable
data object PlacementTestRoute : AppRoute {
    override val screenName: String = "PlacementTest"
}
```

---

## 2. Server REST API Contracts (OpenAPI 3.0.3)

### Endpoint 1: Get Questions (Start Session)
- **Method**: `GET`
- **Path**: `/api/v2/placement/questions`
- **Auth**: Bearer JWT (Optional/Permissive for guests)
- **Response 200 OK**:
```json
{
  "status": "success",
  "data": {
    "sessionId": "sess_89f2a71d-...",
    "totalQuestions": 20,
    "questions": [
      {
        "id": "pq_a1_01",
        "questionText": "I _____ from London. Nice to meet you.",
        "options": ["am", "is", "are", "be"],
        "level": "A1",
        "category": "grammar"
      }
    ]
  }
}
```

### Endpoint 2: Evaluate Answers
- **Method**: `POST`
- **Path**: `/api/v2/placement/evaluate`
- **Auth**: Bearer JWT (Optional/Permissive for guests)
- **Request Body**:
```json
{
  "sessionId": "sess_89f2a71d-...",
  "answers": {
    "pq_a1_01": "am",
    "pq_a1_02": "goes"
  }
}
```
- **Response 200 OK**:
```json
{
  "status": "success",
  "data": {
    "sessionId": "sess_89f2a71d-...",
    "assignedLevel": "B1",
    "scorePercent": 65,
    "correctCount": 13,
    "wrongCount": 7,
    "totalCount": 20,
    "levelTitleFa": "متوسط (Intermediate)",
    "descriptionFa": "شما توانایی برقراری مکالمات روان در موضوعات روزمره، سفر و محیط کاری را دارید.",
    "reviewItems": [
      {
        "questionId": "pq_a1_01",
        "questionText": "I _____ from London. Nice to meet you.",
        "options": ["am", "is", "are", "be"],
        "selectedOption": "am",
        "correctOption": "am",
        "isCorrect": true,
        "level": "A1"
      }
    ]
  }
}
```

---

## 3. Screen Interface Contract

```kotlin
package ir.aispeaking.feature.placement

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PlacementTestScreen(
    viewModel: PlacementTestViewModel,
    onNavigateBack: () -> Unit,
    onFinishPlacement: (assignedLevel: String) -> Unit,
    modifier: Modifier = Modifier
)
```

---

## 4. UI Actions Contract

```kotlin
package ir.aispeaking.feature.placement

sealed interface PlacementUiAction {
    data class SelectOption(val questionId: String, val option: String) : PlacementUiAction
    data class NextQuestion(val currentIndex: Int) : PlacementUiAction
    data class PreviousQuestion(val currentIndex: Int) : PlacementUiAction
    data object SubmitTest : PlacementUiAction
    data object RetryLoad : PlacementUiAction
    data object RetakeTest : PlacementUiAction
    data object ToggleReviewDialog : PlacementUiAction
}
```
