package ir.aispeaking.roadmap

import androidx.compose.runtime.Stable
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.roadmap.model.LevelData
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState
import kotlinx.collections.immutable.ImmutableList

@Stable
data class RoadmapUiState(
    val isBtnLoading: Boolean = false,
    val isFetchingUser: Boolean = false,
    val user: User? = null,
    val levels: ImmutableList<LevelData> = immutableListOf()
) : UiState


sealed class RoadmapUiEvent : UiEvent {
    data object FetchUser : RoadmapUiEvent()
}

sealed class RoadmapUiNavigation : UiNavigation {
    data object ToBack : RoadmapUiNavigation()
}

typealias OnAction = (RoadmapUiEvent) -> Unit

