package ir.aispeaking.chat

import androidx.compose.runtime.Composable
import ir.aispeaking.domain.model.dictionary.DictionaryEntry
import ir.aispeaking.sharedui.ui.dictionary.DictionaryDetailSheet as SharedDictionaryDetailSheet

/**
 * Chat-facing forwarding composable to [SharedDictionaryDetailSheet].
 * Preserves binary and source compatibility for feature:chat.
 */
@Composable
fun DictionaryDetailSheet(
    visible: Boolean,
    targetWord: String,
    entry: DictionaryEntry? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    isAudioPlaying: Boolean = false,
    isAddedToLeitner: Boolean = false,
    isAddingToLeitner: Boolean = false,
    onDismiss: () -> Unit,
    onRetry: () -> Unit = {},
    onPlayAudio: (url: String) -> Unit = {},
    onStopAudio: () -> Unit = {},
    onAddToLeitner: (entry: DictionaryEntry) -> Unit = {}
) {
    SharedDictionaryDetailSheet(
        visible = visible,
        targetWord = targetWord,
        entry = entry,
        isLoading = isLoading,
        errorMessage = errorMessage,
        isAudioPlaying = isAudioPlaying,
        isAddedToLeitner = isAddedToLeitner,
        isAddingToLeitner = isAddingToLeitner,
        onDismiss = onDismiss,
        onRetry = onRetry,
        onPlayAudio = onPlayAudio,
        onStopAudio = onStopAudio,
        onAddToLeitner = onAddToLeitner
    )
}
