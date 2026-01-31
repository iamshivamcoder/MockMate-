package com.shivams.mockmate.data.repositories

import android.net.Uri
import com.shivams.mockmate.model.analysis.AiVerdict
import com.shivams.mockmate.model.analysis.AnalysisReport
import com.shivams.mockmate.model.analysis.AnalysisResponse
import com.shivams.mockmate.model.analysis.CognitiveTag
import com.shivams.mockmate.model.analysis.InteractionData
import com.shivams.mockmate.model.analysis.QuestionResult
import com.shivams.mockmate.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of PdfAnalysisRepository for development and UI testing.
 * Returns a hardcoded "Modern History" test case with all cognitive tag types represented.
 * 
 * This allows UI development to proceed without consuming API tokens.
 */
@Singleton
class FakePdfAnalysisRepository @Inject constructor() : PdfAnalysisRepository {
    
    private var cachedReport: AnalysisReport? = null
    
    @Suppress("UNUSED_PARAMETER")
    override fun uploadAndAnalyze(contentUri: Uri): Flow<Resource<AnalysisReport>> = flow {
        emit(Resource.Loading)
        
        // Simulate network delay (2-4 seconds for realistic "analyzing" experience)
        delay(2500)
        
        try {
            // Generate mock analysis for "Modern History" test
            val mockResponse = generateMockModernHistoryAnalysis()
            val report = AnalysisReport.fromResponse(mockResponse)
            
            // Cache for potential restoration
            cachedReport = report
            
            emit(Resource.Success(report))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to analyze PDF: ${e.message}", e))
        }
    }
    
    override suspend fun getLastAnalysisReport(): AnalysisReport? = cachedReport
    
    override suspend fun clearCache() {
        cachedReport = null
    }
    
    /**
     * Generates a comprehensive mock analysis for a "Modern History" test.
     * Includes examples of all cognitive tag types for thorough UI testing.
     */
    private fun generateMockModernHistoryAnalysis(): AnalysisResponse {
        return AnalysisResponse(
            testSubject = "Modern History - Indian National Movement",
            totalQuestions = 10,
            analysisTimestamp = System.currentTimeMillis(),
            questions = listOf(
                // Q1: SOLID - Confident correct answer
                QuestionResult(
                    questionNumber = 1,
                    interactionData = InteractionData(
                        inkColorsUsed = listOf("blue"),
                        eliminationAttempts = 0,
                        timeSpentSeconds = 45,
                        strikethroughCount = 0
                    ),
                    aiVerdict = AiVerdict(
                        isCorrect = true,
                        cognitiveTag = CognitiveTag.SOLID,
                        reasoning = "Clean, confident approach. Student directly marked the answer about Gandhi's role in Salt March without hesitation.",
                        confidenceScore = 0.95f
                    )
                ),
                
                // Q2: CONCEPT_COLLAPSE - High effort, wrong answer
                QuestionResult(
                    questionNumber = 2,
                    interactionData = InteractionData(
                        inkColorsUsed = listOf("blue", "red"),
                        eliminationAttempts = 3,
                        timeSpentSeconds = 120,
                        strikethroughCount = 3
                    ),
                    aiVerdict = AiVerdict(
                        isCorrect = false,
                        cognitiveTag = CognitiveTag.CONCEPT_COLLAPSE,
                        reasoning = "Student eliminated 3 options systematically but confused the Rowlatt Act with the Vernacular Press Act. Needs concept clarification on colonial-era legislation.",
                        confidenceScore = 0.88f
                    )
                ),
                
                // Q3: INTUITION - Quick correct answer
                QuestionResult(
                    questionNumber = 3,
                    interactionData = InteractionData(
                        inkColorsUsed = listOf("blue"),
                        eliminationAttempts = 0,
                        timeSpentSeconds = 15,
                        strikethroughCount = 0
                    ),
                    aiVerdict = AiVerdict(
                        isCorrect = true,
                        cognitiveTag = CognitiveTag.INTUITION,
                        reasoning = "Rapid answer selection for Quit India Movement date. Student's quick recall suggests strong foundational knowledge.",
                        confidenceScore = 0.82f
                    )
                ),
                
                // Q4: FLUKE - Quick wrong answer
                QuestionResult(
                    questionNumber = 4,
                    interactionData = InteractionData(
                        inkColorsUsed = listOf("blue"),
                        eliminationAttempts = 0,
                        timeSpentSeconds = 10,
                        strikethroughCount = 0
                    ),
                    aiVerdict = AiVerdict(
                        isCorrect = false,
                        cognitiveTag = CognitiveTag.FLUKE,
                        reasoning = "Hasty selection without analysis. Confused Subhas Chandra Bose's INA formation year. Likely a careless error.",
                        confidenceScore = 0.75f
                    )
                ),
                
                // Q5: DOUBT - Brown ink showing uncertainty
                QuestionResult(
                    questionNumber = 5,
                    interactionData = InteractionData(
                        inkColorsUsed = listOf("blue", "brown"),
                        eliminationAttempts = 1,
                        timeSpentSeconds = 90,
                        strikethroughCount = 1
                    ),
                    aiVerdict = AiVerdict(
                        isCorrect = true,
                        cognitiveTag = CognitiveTag.DOUBT,
                        reasoning = "Brown ink annotations show uncertainty about the Lucknow Pact details. Eventually correct but self-doubt markers present.",
                        confidenceScore = 0.70f
                    )
                ),
                
                // Q6: SOLID - Another confident correct
                QuestionResult(
                    questionNumber = 6,
                    interactionData = InteractionData(
                        inkColorsUsed = listOf("blue"),
                        eliminationAttempts = 0,
                        timeSpentSeconds = 30,
                        strikethroughCount = 0
                    ),
                    aiVerdict = AiVerdict(
                        isCorrect = true,
                        cognitiveTag = CognitiveTag.SOLID,
                        reasoning = "Clear understanding of the Non-Cooperation Movement's chronology and key events.",
                        confidenceScore = 0.92f
                    )
                ),
                
                // Q7: CONCEPT_COLLAPSE - Another high effort wrong
                QuestionResult(
                    questionNumber = 7,
                    interactionData = InteractionData(
                        inkColorsUsed = listOf("blue", "red", "brown"),
                        eliminationAttempts = 2,
                        timeSpentSeconds = 150,
                        strikethroughCount = 4
                    ),
                    aiVerdict = AiVerdict(
                        isCorrect = false,
                        cognitiveTag = CognitiveTag.CONCEPT_COLLAPSE,
                        reasoning = "Extensive deliberation visible. Mixed up the Cabinet Mission Plan provisions. Multiple revisions indicate conceptual confusion about constitutional developments.",
                        confidenceScore = 0.91f
                    )
                ),
                
                // Q8: INTUITION - Quick correct
                QuestionResult(
                    questionNumber = 8,
                    interactionData = InteractionData(
                        inkColorsUsed = listOf("blue"),
                        eliminationAttempts = 0,
                        timeSpentSeconds = 20,
                        strikethroughCount = 0
                    ),
                    aiVerdict = AiVerdict(
                        isCorrect = true,
                        cognitiveTag = CognitiveTag.INTUITION,
                        reasoning = "Immediate recognition of Jallianwala Bagh massacre date. Strong episodic memory.",
                        confidenceScore = 0.85f
                    )
                ),
                
                // Q9: DOUBT - Uncertain but wrong
                QuestionResult(
                    questionNumber = 9,
                    interactionData = InteractionData(
                        inkColorsUsed = listOf("blue", "brown"),
                        eliminationAttempts = 2,
                        timeSpentSeconds = 100,
                        strikethroughCount = 2
                    ),
                    aiVerdict = AiVerdict(
                        isCorrect = false,
                        cognitiveTag = CognitiveTag.DOUBT,
                        reasoning = "Heavy brown ink usage shows awareness of uncertainty. Confused different sessions of Indian National Congress.",
                        confidenceScore = 0.68f
                    )
                ),
                
                // Q10: SOLID - Confident finish
                QuestionResult(
                    questionNumber = 10,
                    interactionData = InteractionData(
                        inkColorsUsed = listOf("blue"),
                        eliminationAttempts = 1,
                        timeSpentSeconds = 40,
                        strikethroughCount = 1
                    ),
                    aiVerdict = AiVerdict(
                        isCorrect = true,
                        cognitiveTag = CognitiveTag.SOLID,
                        reasoning = "Systematic elimination led to correct answer about the Cripps Mission. Good analytical approach.",
                        confidenceScore = 0.89f
                    )
                )
            )
        )
    }
}
