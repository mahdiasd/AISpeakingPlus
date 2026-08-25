package ir.aispeaking.roadmap.model

import androidx.compose.runtime.Stable

@Stable
data class LevelData(
    val level: Int,
    val title: String,
    val rangeText: String,
    val isPassed: Boolean,
)