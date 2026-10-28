package ir.aispeaking.sharedui.ui.guide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect

/**
 * State controller for managing multi-step guides and spotlights on a screen.
 */
@Stable
class GuideState(
    val screenKey: String,
    val onCompleted: (screenKey: String) -> Unit = {}
) {
    var isVisible by mutableStateOf(false)
        private set

    var currentStepIndex by mutableStateOf(0)
        private set

    private var steps: List<GuideStep> = emptyList()

    // Bounding rect for each registered target key in root coordinates
    val targetBounds = mutableStateMapOf<String, Rect>()

    val currentStep: GuideStep?
        get() = steps.getOrNull(currentStepIndex)

    val currentTargetRect: Rect?
        get() = currentStep?.let { targetBounds[it.key] }

    val totalSteps: Int
        get() = steps.size

    val isFirstStep: Boolean
        get() = currentStepIndex == 0

    val isLastStep: Boolean
        get() = currentStepIndex >= steps.size - 1

    fun registerTarget(key: String, bounds: Rect) {
        targetBounds[key] = bounds
    }

    fun unregisterTarget(key: String) {
        targetBounds.remove(key)
    }

    fun start(guideSteps: List<GuideStep>) {
        if (guideSteps.isEmpty()) return
        steps = guideSteps
        currentStepIndex = 0
        isVisible = true
    }

    fun next() {
        if (currentStepIndex < steps.size - 1) {
            currentStepIndex++
        } else {
            dismiss()
        }
    }

    fun previous() {
        if (currentStepIndex > 0) {
            currentStepIndex--
        }
    }

    fun skip() {
        dismiss()
    }

    fun dismiss() {
        isVisible = false
        onCompleted(screenKey)
    }
}

@Composable
fun rememberGuideState(
    screenKey: String,
    onCompleted: (screenKey: String) -> Unit = {}
): GuideState {
    return remember(screenKey) {
        GuideState(screenKey = screenKey, onCompleted = onCompleted)
    }
}
