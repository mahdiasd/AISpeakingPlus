package ir.aispeaking.domain.model.stage

import kotlinx.serialization.Serializable

/**
 * Locking state for a stage item on the journey map.
 */
@Serializable
enum class StageLockStatus {
    UNLOCKED,
    LOCKED_PREVIOUS_STAGE,    // Needs >= 1 star in previous stage
    LOCKED_REGISTRATION,      // Stage 2 gate (requires free SMS OTP signup)
    LOCKED_SUBSCRIPTION       // Stage 3+ gate (requires active paid subscription)
}
