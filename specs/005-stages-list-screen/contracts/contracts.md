# Contracts: Stages List Screen

**Feature**: `005-stages-list-screen`  
**Date**: 2026-10-07

## 1. Navigation Route Contract

```kotlin
package ir.aispeaking.navigation

import kotlinx.serialization.Serializable

@Serializable
data object StagesListRoute : AppRoute {
    override val screenName: String = "StagesList"
}
```

## 2. Screen Interface Contract

```kotlin
package ir.aispeaking.sharedui.ui.stage.stageslist

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun StagesListScreen(
    viewModel: StagesListViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStageChat: (stageId: String) -> Unit,
    onNavigateToSubscription: () -> Unit,
    modifier: Modifier = Modifier
)
```

## 3. UI Intents & Actions Contract

```kotlin
sealed interface StagesListUiAction {
    data class StageClicked(val stage: Stage) : StagesListUiAction
    data object RetryClicked : StagesListUiAction
    data object RefreshRequested : StagesListUiAction
}
```
