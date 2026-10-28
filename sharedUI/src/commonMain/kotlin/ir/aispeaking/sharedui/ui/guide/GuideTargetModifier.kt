package ir.aispeaking.sharedui.ui.guide

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Modifier to register an element as a target for a spotlight guide step.
 */
fun Modifier.guideTarget(
    key: String,
    state: GuideState
): Modifier = this.then(
    Modifier.onGloballyPositioned { coordinates ->
        if (coordinates.isAttached) {
            val bounds = coordinates.boundsInRoot()
            // Only update if bounds are non-empty and actually visible
            if (bounds.width > 0f && bounds.height > 0f) {
                state.registerTarget(key, bounds)
            }
        }
    }
)
