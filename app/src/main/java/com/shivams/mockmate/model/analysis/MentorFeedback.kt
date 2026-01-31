package com.shivams.mockmate.model.analysis

import com.google.gson.annotations.SerializedName

/**
 * Structured mentor feedback with distinct sections for clarity.
 */
data class MentorFeedback(
    @SerializedName("key_strength")
    val keyStrength: String = "",
    
    @SerializedName("critical_weakness")
    val criticalWeakness: String = "",
    
    @SerializedName("actionable_step")
    val actionableStep: String = ""
) {
    /**
     * Check if this is a legacy plain-text feedback (all fields empty).
     */
    val isStructured: Boolean
        get() = keyStrength.isNotEmpty() || criticalWeakness.isNotEmpty() || actionableStep.isNotEmpty()
    
    companion object {
        /**
         * Parse feedback which could be either a JSON object or a plain string.
         */
        fun fromRawFeedback(raw: String): MentorFeedback {
            // If it looks like JSON object, try to parse it
            if (raw.trim().startsWith("{")) {
                try {
                    val gson = com.google.gson.Gson()
                    return gson.fromJson(raw, MentorFeedback::class.java)
                } catch (e: Exception) {
                    // Fall through to legacy handling
                }
            }
            // Legacy: treat as plain text, put in actionable_step
            return MentorFeedback(actionableStep = raw)
        }
    }
}
