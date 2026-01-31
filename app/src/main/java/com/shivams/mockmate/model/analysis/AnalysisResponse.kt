package com.shivams.mockmate.model.analysis

import com.google.gson.annotations.SerializedName

/**
 * Network response model matching the API JSON Schema.
 * This is the raw response from the PDF analysis endpoint.
 */
data class AnalysisResponse(
    @SerializedName("test_subject")
    val testSubject: String,
    
    @SerializedName("total_questions")
    val totalQuestions: Int,
    
    @SerializedName("analysis_timestamp")
    val analysisTimestamp: Long,
    
    @SerializedName("questions")
    val questions: List<QuestionResult>
)

/**
 * Individual question result from the analysis.
 */
data class QuestionResult(
    @SerializedName("question_number")
    val questionNumber: Int,
    
    @SerializedName("interaction_data")
    val interactionData: InteractionData,
    
    @SerializedName("ai_verdict")
    val aiVerdict: AiVerdict
)

/**
 * Ink behavior data extracted from the PDF.
 * Captures the student's interaction patterns.
 */
data class InteractionData(
    /**
     * Colors of ink detected on this question.
     * Examples: ["blue"], ["blue", "red"], ["blue", "brown"]
     * - Blue = Standard work
     * - Red = Elimination attempts
     * - Brown = Doubt/uncertainty markers
     */
    @SerializedName("ink_colors_used")
    val inkColorsUsed: List<String>,
    
    /**
     * Number of options that were crossed out/eliminated.
     */
    @SerializedName("elimination_attempts")
    val eliminationAttempts: Int,
    
    /**
     * Time spent on this question in seconds (if available from annotations).
     */
    @SerializedName("time_spent_seconds")
    val timeSpentSeconds: Int? = null,
    
    /**
     * Number of strikethrough marks detected.
     */
    @SerializedName("strikethrough_count")
    val strikethroughCount: Int
)

/**
 * AI-generated verdict for a question.
 */
data class AiVerdict(
    /**
     * Whether the final answer was correct.
     */
    @SerializedName("is_correct")
    val isCorrect: Boolean,
    
    /**
     * The cognitive diagnosis tag.
     */
    @SerializedName("cognitive_tag")
    val cognitiveTag: CognitiveTag,
    
    /**
     * AI's reasoning for the diagnosis.
     * Explains why this particular tag was assigned.
     */
    @SerializedName("reasoning")
    val reasoning: String,
    
    /**
     * Confidence score for this diagnosis (0.0 - 1.0).
     */
    @SerializedName("confidence_score")
    val confidenceScore: Float
)
