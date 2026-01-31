package com.shivams.mockmate.model.analysis

/**
 * Represents the cognitive diagnosis categories for PDF analysis.
 * Based on the "MockMate Legend" for ink colors and answer behaviors.
 */
enum class CognitiveTag {
    /**
     * SOLID - Competence indicator
     * Correct answer with confident, methodical approach.
     * Blue ink with clear reasoning path.
     */
    SOLID,

    /**
     * CONCEPT_COLLAPSE - High effort but wrong
     * Student showed significant work/elimination attempts but arrived at wrong answer.
     * Indicates fundamental misunderstanding that needs concept review.
     */
    CONCEPT_COLLAPSE,

    /**
     * INTUITION - Low effort and right
     * Quick, confident answer that turned out correct.
     * Good instinct - may indicate strong foundational knowledge.
     */
    INTUITION,

    /**
     * FLUKE - Low effort and wrong
     * Quick answer without much thought that was incorrect.
     * Potential lucky guess candidate or careless error.
     */
    FLUKE,

    /**
     * DOUBT - Brown ink usage indicator
     * Student showed uncertainty through brown ink annotations.
     * Needs attention regardless of final answer correctness.
     */
    DOUBT
}
