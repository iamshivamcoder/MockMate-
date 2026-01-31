package com.shivams.mockmate.model.analysis

/**
 * Clean domain entity representing a single question's cognitive analysis.
 * Maps from the raw network response to a usable domain model.
 */
data class QuestionAnalysis(
    val questionNumber: Int,
    val cognitiveTag: CognitiveTag,
    val isCorrect: Boolean,
    val reasoning: String,
    val confidenceScore: Float,
    val inkColorUsed: List<String>,
    val eliminationAttempts: Int,
    val strikethroughCount: Int,
    val timeSpentSeconds: Int? = null
) {
    /**
     * Indicates if this answer might be a fluke (lucky guess).
     * True if explicitly tagged as FLUKE, or if it was an intuitive answer
     * but with low AI confidence.
     */
    val isFluke: Boolean
        get() = cognitiveTag == CognitiveTag.FLUKE ||
                (cognitiveTag == CognitiveTag.INTUITION && confidenceScore < 0.6f)

    /**
     * Indicates if this question needs the student's attention for review.
     * True for concept collapse (fundamental misunderstanding) or doubt markers.
     */
    val needsReview: Boolean
        get() = cognitiveTag == CognitiveTag.CONCEPT_COLLAPSE ||
                cognitiveTag == CognitiveTag.DOUBT

    /**
     * The primary (first) ink color used on this question.
     */
    val primaryInkColor: String?
        get() = inkColorUsed.firstOrNull()

    /**
     * Whether the student showed doubt (used brown ink).
     */
    val showedDoubt: Boolean
        get() = inkColorUsed.contains("brown")

    /**
     * Whether the student attempted elimination (used red ink).
     */
    val usedElimination: Boolean
        get() = inkColorUsed.contains("red") || eliminationAttempts > 0

    companion object {
        /**
         * Factory function to create QuestionAnalysis from API response models.
         */
        fun fromQuestionResult(result: QuestionResult): QuestionAnalysis {
            return QuestionAnalysis(
                questionNumber = result.questionNumber,
                cognitiveTag = result.aiVerdict.cognitiveTag,
                isCorrect = result.aiVerdict.isCorrect,
                reasoning = result.aiVerdict.reasoning,
                confidenceScore = result.aiVerdict.confidenceScore,
                inkColorUsed = result.interactionData.inkColorsUsed,
                eliminationAttempts = result.interactionData.eliminationAttempts,
                strikethroughCount = result.interactionData.strikethroughCount,
                timeSpentSeconds = result.interactionData.timeSpentSeconds
            )
        }
    }
}
