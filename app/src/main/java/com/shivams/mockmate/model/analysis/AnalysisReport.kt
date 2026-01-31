package com.shivams.mockmate.model.analysis

/**
 * Aggregated analysis report for UI display.
 * Contains the full list of analyzed questions plus computed summary statistics.
 */
data class AnalysisReport(
    val testSubject: String,
    val totalQuestions: Int,
    val analysisTimestamp: Long,
    val questions: List<QuestionAnalysis>,
    val summary: AnalysisSummary,
    val mentorFeedback: String = ""
) {
    companion object {
        /**
         * Factory function to create AnalysisReport from API response.
         */
        fun fromResponse(response: AnalysisResponse, mentorFeedback: String = ""): AnalysisReport {
            val questions = response.questions.map { QuestionAnalysis.fromQuestionResult(it) }
            return AnalysisReport(
                testSubject = response.testSubject,
                totalQuestions = response.totalQuestions,
                analysisTimestamp = response.analysisTimestamp,
                questions = questions,
                summary = AnalysisSummary.fromQuestions(questions),
                mentorFeedback = mentorFeedback
            )
        }
    }
}

/**
 * Summary statistics computed from the analyzed questions.
 * Used for dashboard display and quick insights.
 */
data class AnalysisSummary(
    val solidCount: Int,
    val conceptCollapseCount: Int,
    val intuitionCount: Int,
    val flukeCount: Int,
    val doubtCount: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val overallAccuracy: Float,
    val intuitionAccuracy: Float  // Success rate when brown ink (doubt) was used
) {
    /**
     * Questions that need immediate attention (Concept Collapse + Doubt).
     */
    val immediateActionCount: Int
        get() = conceptCollapseCount + doubtCount

    /**
     * Percentage of questions showing confident, solid understanding.
     */
    val solidPercentage: Float
        get() = if (solidCount + conceptCollapseCount + intuitionCount + flukeCount + doubtCount > 0) {
            solidCount.toFloat() / (solidCount + conceptCollapseCount + intuitionCount + flukeCount + doubtCount)
        } else 0f

    companion object {
        /**
         * Compute summary statistics from a list of question analyses.
         */
        fun fromQuestions(questions: List<QuestionAnalysis>): AnalysisSummary {
            val solidCount = questions.count { it.cognitiveTag == CognitiveTag.SOLID }
            val conceptCollapseCount = questions.count { it.cognitiveTag == CognitiveTag.CONCEPT_COLLAPSE }
            val intuitionCount = questions.count { it.cognitiveTag == CognitiveTag.INTUITION }
            val flukeCount = questions.count { it.cognitiveTag == CognitiveTag.FLUKE }
            val doubtCount = questions.count { it.cognitiveTag == CognitiveTag.DOUBT }
            
            val correctCount = questions.count { it.isCorrect }
            val incorrectCount = questions.size - correctCount
            
            val overallAccuracy = if (questions.isNotEmpty()) {
                correctCount.toFloat() / questions.size
            } else 0f
            
            // Intuition accuracy: how often "doubted" answers were correct
            val doubtQuestions = questions.filter { it.showedDoubt }
            val intuitionAccuracy = if (doubtQuestions.isNotEmpty()) {
                doubtQuestions.count { it.isCorrect }.toFloat() / doubtQuestions.size
            } else 0f
            
            return AnalysisSummary(
                solidCount = solidCount,
                conceptCollapseCount = conceptCollapseCount,
                intuitionCount = intuitionCount,
                flukeCount = flukeCount,
                doubtCount = doubtCount,
                correctCount = correctCount,
                incorrectCount = incorrectCount,
                overallAccuracy = overallAccuracy,
                intuitionAccuracy = intuitionAccuracy
            )
        }
    }
}
