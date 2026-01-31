"""
MockMate Metacognitive PDF Analyzer Backend
FastAPI server that processes annotated PDFs and returns cognitive analysis.

Run with:
    pip install fastapi uvicorn python-multipart
    uvicorn main:app --reload --host 0.0.0.0 --port 8000

Endpoint:
    POST /analyze - Upload PDF and get cognitive analysis
"""

import time
from enum import Enum
from typing import List, Optional
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

app = FastAPI(
    title="MockMate PDF Analyzer",
    description="Metacognitive analysis of annotated test PDFs",
    version="1.0.0"
)

# Allow CORS for Android app
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ==================== Models ====================

class CognitiveTag(str, Enum):
    """Cognitive diagnosis categories matching Android app enum."""
    SOLID = "SOLID"
    CONCEPT_COLLAPSE = "CONCEPT_COLLAPSE"
    INTUITION = "INTUITION"
    FLUKE = "FLUKE"
    DOUBT = "DOUBT"


class InteractionData(BaseModel):
    """Ink behavior data extracted from the PDF."""
    ink_colors_used: List[str] = Field(..., description="Colors detected: blue, red, brown")
    elimination_attempts: int = Field(..., description="Number of options crossed out")
    time_spent_seconds: Optional[int] = Field(None, description="Time spent on question")
    strikethrough_count: int = Field(..., description="Number of strikethroughs")


class AiVerdict(BaseModel):
    """AI-generated verdict for a question."""
    is_correct: bool = Field(..., description="Whether the answer was correct")
    cognitive_tag: CognitiveTag = Field(..., description="Cognitive diagnosis")
    reasoning: str = Field(..., description="AI explanation for the tag")
    confidence_score: float = Field(..., ge=0.0, le=1.0, description="Confidence 0-1")


class QuestionResult(BaseModel):
    """Individual question analysis result."""
    question_number: int
    interaction_data: InteractionData
    ai_verdict: AiVerdict


class AnalysisResponse(BaseModel):
    """Complete analysis response - matches Android AnalysisResponse.kt"""
    test_subject: str
    total_questions: int
    analysis_timestamp: int = Field(..., description="Unix timestamp in milliseconds")
    questions: List[QuestionResult]


# ==================== Mock Data ====================

def generate_mock_analysis() -> AnalysisResponse:
    """
    Generate the same mock data as FakePdfAnalysisRepository.kt
    Modern History - Indian National Movement test with 10 questions.
    """
    return AnalysisResponse(
        test_subject="Modern History - Indian National Movement",
        total_questions=10,
        analysis_timestamp=int(time.time() * 1000),  # Current time in ms
        questions=[
            # Q1: SOLID - Confident correct answer
            QuestionResult(
                question_number=1,
                interaction_data=InteractionData(
                    ink_colors_used=["blue"],
                    elimination_attempts=0,
                    time_spent_seconds=45,
                    strikethrough_count=0
                ),
                ai_verdict=AiVerdict(
                    is_correct=True,
                    cognitive_tag=CognitiveTag.SOLID,
                    reasoning="Clean, confident approach. Student directly marked the answer about Gandhi's role in Salt March without hesitation.",
                    confidence_score=0.95
                )
            ),
            # Q2: CONCEPT_COLLAPSE - High effort, wrong answer
            QuestionResult(
                question_number=2,
                interaction_data=InteractionData(
                    ink_colors_used=["blue", "red"],
                    elimination_attempts=3,
                    time_spent_seconds=120,
                    strikethrough_count=3
                ),
                ai_verdict=AiVerdict(
                    is_correct=False,
                    cognitive_tag=CognitiveTag.CONCEPT_COLLAPSE,
                    reasoning="Student eliminated 3 options systematically but confused the Rowlatt Act with the Vernacular Press Act. Needs concept clarification on colonial-era legislation.",
                    confidence_score=0.88
                )
            ),
            # Q3: INTUITION - Quick correct answer
            QuestionResult(
                question_number=3,
                interaction_data=InteractionData(
                    ink_colors_used=["blue"],
                    elimination_attempts=0,
                    time_spent_seconds=15,
                    strikethrough_count=0
                ),
                ai_verdict=AiVerdict(
                    is_correct=True,
                    cognitive_tag=CognitiveTag.INTUITION,
                    reasoning="Rapid answer selection for Quit India Movement date. Student's quick recall suggests strong foundational knowledge.",
                    confidence_score=0.82
                )
            ),
            # Q4: FLUKE - Quick wrong answer
            QuestionResult(
                question_number=4,
                interaction_data=InteractionData(
                    ink_colors_used=["blue"],
                    elimination_attempts=0,
                    time_spent_seconds=10,
                    strikethrough_count=0
                ),
                ai_verdict=AiVerdict(
                    is_correct=False,
                    cognitive_tag=CognitiveTag.FLUKE,
                    reasoning="Hasty selection without analysis. Confused Subhas Chandra Bose's INA formation year. Likely a careless error.",
                    confidence_score=0.75
                )
            ),
            # Q5: DOUBT - Brown ink showing uncertainty
            QuestionResult(
                question_number=5,
                interaction_data=InteractionData(
                    ink_colors_used=["blue", "brown"],
                    elimination_attempts=1,
                    time_spent_seconds=90,
                    strikethrough_count=1
                ),
                ai_verdict=AiVerdict(
                    is_correct=True,
                    cognitive_tag=CognitiveTag.DOUBT,
                    reasoning="Brown ink annotations show uncertainty about the Lucknow Pact details. Eventually correct but self-doubt markers present.",
                    confidence_score=0.70
                )
            ),
            # Q6: SOLID - Another confident correct
            QuestionResult(
                question_number=6,
                interaction_data=InteractionData(
                    ink_colors_used=["blue"],
                    elimination_attempts=0,
                    time_spent_seconds=30,
                    strikethrough_count=0
                ),
                ai_verdict=AiVerdict(
                    is_correct=True,
                    cognitive_tag=CognitiveTag.SOLID,
                    reasoning="Clear understanding of the Non-Cooperation Movement's chronology and key events.",
                    confidence_score=0.92
                )
            ),
            # Q7: CONCEPT_COLLAPSE - Another high effort wrong
            QuestionResult(
                question_number=7,
                interaction_data=InteractionData(
                    ink_colors_used=["blue", "red", "brown"],
                    elimination_attempts=2,
                    time_spent_seconds=150,
                    strikethrough_count=4
                ),
                ai_verdict=AiVerdict(
                    is_correct=False,
                    cognitive_tag=CognitiveTag.CONCEPT_COLLAPSE,
                    reasoning="Extensive deliberation visible. Mixed up the Cabinet Mission Plan provisions. Multiple revisions indicate conceptual confusion about constitutional developments.",
                    confidence_score=0.91
                )
            ),
            # Q8: INTUITION - Quick correct
            QuestionResult(
                question_number=8,
                interaction_data=InteractionData(
                    ink_colors_used=["blue"],
                    elimination_attempts=0,
                    time_spent_seconds=20,
                    strikethrough_count=0
                ),
                ai_verdict=AiVerdict(
                    is_correct=True,
                    cognitive_tag=CognitiveTag.INTUITION,
                    reasoning="Immediate recognition of Jallianwala Bagh massacre date. Strong episodic memory.",
                    confidence_score=0.85
                )
            ),
            # Q9: DOUBT - Uncertain but wrong
            QuestionResult(
                question_number=9,
                interaction_data=InteractionData(
                    ink_colors_used=["blue", "brown"],
                    elimination_attempts=2,
                    time_spent_seconds=100,
                    strikethrough_count=2
                ),
                ai_verdict=AiVerdict(
                    is_correct=False,
                    cognitive_tag=CognitiveTag.DOUBT,
                    reasoning="Heavy brown ink usage shows awareness of uncertainty. Confused different sessions of Indian National Congress.",
                    confidence_score=0.68
                )
            ),
            # Q10: SOLID - Confident finish
            QuestionResult(
                question_number=10,
                interaction_data=InteractionData(
                    ink_colors_used=["blue"],
                    elimination_attempts=1,
                    time_spent_seconds=40,
                    strikethrough_count=1
                ),
                ai_verdict=AiVerdict(
                    is_correct=True,
                    cognitive_tag=CognitiveTag.SOLID,
                    reasoning="Systematic elimination led to correct answer about the Cripps Mission. Good analytical approach.",
                    confidence_score=0.89
                )
            ),
        ]
    )


# ==================== Endpoints ====================

@app.get("/")
async def root():
    """Health check endpoint."""
    return {"status": "ok", "service": "MockMate PDF Analyzer"}


@app.post("/analyze", response_model=AnalysisResponse)
async def analyze_pdf(file: UploadFile = File(...)):
    """
    Analyze an annotated PDF and return cognitive diagnosis.
    
    Currently returns mock data. In production, this would:
    1. Parse PDF annotations
    2. Extract ink colors and strikethroughs
    3. Send to AI for cognitive analysis
    4. Return structured response
    """
    # Validate file type
    if not file.content_type == "application/pdf":
        raise HTTPException(
            status_code=400,
            detail=f"Invalid file type: {file.content_type}. Expected application/pdf"
        )
    
    # Read file (for future processing)
    contents = await file.read()
    file_size_kb = len(contents) / 1024
    
    print(f"📄 Received PDF: {file.filename} ({file_size_kb:.1f} KB)")
    
    # TODO: Actual PDF processing here
    # For now, return mock data
    
    # Simulate processing delay (remove in production)
    import asyncio
    await asyncio.sleep(1.5)
    
    return generate_mock_analysis()


@app.get("/analyze/sample", response_model=AnalysisResponse)
async def get_sample_analysis():
    """
    Get sample analysis without uploading a file.
    Useful for testing the Android app.
    """
    return generate_mock_analysis()


# ==================== Run Server ====================

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
