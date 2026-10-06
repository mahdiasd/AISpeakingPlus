package ir.aispeaking.domain.model.stage

import kotlinx.serialization.Serializable

/**
 * Access tier for the speaking journey.
 */
@Serializable
enum class AccessTier {
    GUEST,           // Tier 1: Can play Stage 1 only
    REGISTERED_FREE, // Tier 2: Can play Stage 1 and Stage 2
    SUBSCRIBER       // Tier 3: Can play all stages (Stage 1..15+)
}
