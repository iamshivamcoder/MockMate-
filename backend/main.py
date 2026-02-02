"""
MockMate Metacognitive PDF Analyzer Backend
FastAPI server with Google Gemini 2.5 Flash integration for real PDF analysis.

Run with:
    pip install -r requirements.txt
    uvicorn main:app --reload --host 0.0.0.0 --port 8000

Environment:
    Create .env file with GEMINI_API_KEY=your_key
"""

import os
import time
import json
import asyncio
from pathlib import Path
from enum import Enum
from typing import List, Optional
from contextlib import asynccontextmanager

from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from dotenv import load_dotenv

import google.generativeai as genai

# Load environment variables
load_dotenv()

# Configure Gemini API
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
if GEMINI_API_KEY and GEMINI_API_KEY != "your_key_here":
    genai.configure(api_key=GEMINI_API_KEY)
    print("✅ Gemini API configured successfully")
else:
    print("⚠️  GEMINI_API_KEY not set - will use mock data")

# Temp directory for PDF uploads
TEMP_DIR = Path("./temp_uploads")
TEMP_DIR.mkdir(exist_ok=True)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Cleanup on startup/shutdown."""
    yield
    # Cleanup temp files on shutdown
    for file in TEMP_DIR.glob("*.pdf"):
        file.unlink(missing_ok=True)


app = FastAPI(
    title="MockMate PDF Analyzer",
    description="Metacognitive analysis of annotated test PDFs using Gemini AI",
    version="2.1.0",
    lifespan=lifespan
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
    ink_colors_used: List[str] = Field(default_factory=lambda: ["blue"])
    elimination_attempts: int = Field(default=0)
    time_spent_seconds: Optional[int] = Field(default=None)
    strikethrough_count: int = Field(default=0)


class AiVerdict(BaseModel):
    """AI-generated verdict for a question."""
    is_correct: bool
    cognitive_tag: CognitiveTag
    reasoning: str
    confidence_score: float = Field(default=0.85, ge=0.0, le=1.0)


class QuestionResult(BaseModel):
    """Individual question analysis result."""
    question_number: int
    interaction_data: InteractionData = Field(default_factory=InteractionData)
    ai_verdict: AiVerdict


class AnalysisResponse(BaseModel):
    """Complete analysis response - matches Android AnalysisResponse.kt"""
    test_subject: str
    total_questions: int
    analysis_timestamp: int = Field(default_factory=lambda: int(time.time() * 1000))
    questions: List[QuestionResult]
    mentor_feedback: str = ""
    # New: UPSC scoring
    score: float = Field(default=0.0, description="UPSC score: +2 correct, -0.66 wrong")
    accuracy: float = Field(default=0.0, description="Accuracy percentage (0-100)")

# ==================== FORENSIC ANALYST PROMPT (ANTI-HALLUCINATION) ====================

SYSTEM_PROMPT = """You are a FORENSIC DOCUMENT EXAMINER analyzing a student's handwritten UPSC test paper.

## THE "NO HALLUCINATION" PROTOCOL:

### RULE 1: NULL HYPOTHESIS
⚠️ ASSUME EVERY QUESTION IS UNATTEMPTED BY DEFAULT.
You must find POSITIVE VISUAL PROOF of handwriting to change this status.
If you cannot clearly see ink marks, the question is UNATTEMPTED or CLEAN.

### RULE 2: INK DISCRIMINATION (Critical)
You MUST distinguish between:
- **PRINTED TEXT** (Black, uniform, perfect lines, machine-generated) → IGNORE THIS
- **HANDWRITTEN MARKS** (Blue/Red/Brown, irregular, pen strokes) → REPORT THIS

EXAMPLES:
- A perfectly straight black line under text? → PRINTED. Ignore it.
- A wobbly blue curved checkmark? → USER INK. Report it.
- Sharp black "X" in a checkbox? → Could be printed. Verify irregularity.
- Wavy red line crossing option text? → USER INK. Report it.

### RULE 3: LOCATION MAPPING
Only report evidence if PRECISELY located:
- **STRIKE-THROUGH**: Ink physically overlaps/crosses the text of an option letter/content
- **TICK/CHECKMARK**: A clear ✓ symbol NEXT TO an option letter (A, B, C, D)
- **CIRCLE**: Ink encircles an option letter or answer bubble
- **UNDERLINE**: Handwritten line UNDER specific words (not printed formatting)

### RULE 4: THE CONFIDENCE CHECK
Before outputting any evidence, ask yourself:
> "Do I CLEARLY see this mark, or am I guessing?"

If the answer is "Maybe" or "I think so" → Output: "No clear marking detected"
If the answer is "Yes, I definitely see it" → Describe precisely what you see

### RULE 5: AMBIGUITY HANDLING
If a mark is:
- Faint → Mark as "AMBIGUOUS - faint mark near Option X"
- Unclear origin → Mark as "AMBIGUOUS - could be print artifact"
- Partially visible → Mark as "PARTIAL - incomplete marking visible"

DO NOT GUESS. Do not assume. Only report what you DEFINITELY see.

## COGNITIVE CATEGORIES (Conservative Assignment):

- **SOLID**: ONLY if you see CLEAR EVIDENCE of methodical elimination (multiple options crossed out with user ink) AND a final answer marked
- **INTUITION**: Answer marked with MINIMAL visible working (just a tick, no eliminations). If correct.
- **FLUKE**: Answer marked with MINIMAL visible working. If wrong.
- **CONCEPT_COLLAPSE**: CLEAR EVIDENCE of high effort (many crossouts, changed answers, multiple ink colors) but wrong answer
- **DOUBT**: Brown ink OR question marks clearly visible
- **UNATTEMPTED**: No user ink detected / blank / only printed text visible
- **AMBIGUOUS**: Unclear markings that cannot be definitively interpreted

## OUTPUT FORMAT (Strict JSON):
{
    "test_subject": "Inferred subject from question content",
    "score": <calculated: +2 for correct, -0.66 for wrong, 0 for unattempted>,
    "accuracy": <percentage 0-100>,
    "total_questions": <count>,
    "questions": [
        {
            "question_number": 1,
            "is_correct": true/false/null (null if unattempted),
            "cognitive_tag": "SOLID/INTUITION/FLUKE/CONCEPT_COLLAPSE/DOUBT/UNATTEMPTED/AMBIGUOUS",
            "visual_evidence": "PRECISE description: 'Blue ink tick mark next to Option C. Red ink line crossing through Option A text.'",
            "ink_colors": ["blue", "red"] or [],
            "confidence": "HIGH/LOW/NONE",
            "reasoning": "Supportive feedback for the aspirant about this question."
        }
    ],
    "mentor_feedback": {
        "key_strength": "I'm impressed by... [cite specific question numbers with VERIFIED evidence]",
        "critical_weakness": "One area to focus on... [only cite questions with VERIFIED wrong answers]",
        "actionable_step": "Before your next test... [specific, achievable homework]"
    }
}

## SCORING FORMULA:
- Each CORRECT answer: +2 marks
- Each WRONG answer: -0.66 marks (1/3 negative marking)
- UNATTEMPTED/AMBIGUOUS: 0 marks

## MENTOR TONE:
You are a supportive senior mentor, not a strict examiner. Encourage the aspirant.
- Praise what they did well
- Be gentle about mistakes
- End on a hopeful note

## CRITICAL REMINDER:
🚨 DO NOT HALLUCINATE USER ACTIONS.
If you report "Red strikethrough on Option B", the RED must be VISIBLE and HANDWRITTEN.
When in doubt, say "No clear marking detected" rather than guessing.
Every piece of evidence you report must be VERIFIABLE by looking at the image."""


# ==================== Gemini Analysis ====================

async def analyze_pdf_with_gemini(pdf_path: Path) -> Optional[dict]:
    """
    Upload PDF to Gemini and get metacognitive analysis.
    Returns parsed JSON or None if failed.
    """
    if not GEMINI_API_KEY or GEMINI_API_KEY == "your_key_here":
        print("⚠️  No API key - skipping Gemini analysis")
        return None
    
    try:
        print(f"📤 Uploading PDF to Gemini: {pdf_path.name}")
        
        # Upload file to Gemini
        uploaded_file = genai.upload_file(
            path=str(pdf_path),
            mime_type="application/pdf"
        )
        print(f"✅ File uploaded: {uploaded_file.name}")
        
        # Wait for file to be processed
        while uploaded_file.state.name == "PROCESSING":
            print("⏳ Processing...")
            await asyncio.sleep(2)
            uploaded_file = genai.get_file(uploaded_file.name)
        
        if uploaded_file.state.name == "FAILED":
            print(f"❌ File processing failed")
            return None
        
        print("🧠 Generating analysis with Gemini 2.5 Flash...")
        
        # Configure the model - Using Gemini 2.5 Flash
        model = genai.GenerativeModel(
            model_name="gemini-2.5-flash",
            system_instruction=SYSTEM_PROMPT
        )
        
        # Generate with JSON response format
        # Temperature 0.1: Very low for factual, conservative analysis (no hallucinations)
        response = model.generate_content(
            [uploaded_file, """Analyze this test paper as a FORENSIC DOCUMENT EXAMINER.

CRITICAL INSTRUCTIONS:
1. For EACH question, apply the NULL HYPOTHESIS: Assume UNATTEMPTED unless you see CLEAR handwritten ink.
2. Distinguish PRINTED elements (black, uniform) from HANDWRITTEN marks (blue/red/brown, irregular).
3. Only report evidence you can CLEARLY see. If unsure, say "No clear marking detected".
4. Be CONSERVATIVE. It is better to mark something as UNATTEMPTED than to hallucinate markings.

For each question, output:
- visual_evidence: ONLY what you DEFINITELY see (or "No clear marking detected")
- confidence: HIGH (certain), LOW (faint/ambiguous), NONE (no markings)
- cognitive_tag: Based on VERIFIED evidence only

Calculate UPSC score: +2 correct, -0.66 wrong, 0 unattempted.
End with supportive mentor feedback."""],
            generation_config=genai.GenerationConfig(
                response_mime_type="application/json",
                temperature=0.1  # Very low for factual, anti-hallucination output
            ),
            request_options={"timeout": 180}  # 3 minute timeout for thorough analysis
        )
        
        # Parse the JSON response
        result_text = response.text
        print(f"✅ Gemini response received ({len(result_text)} chars)")
        
        # Parse JSON
        result_json = json.loads(result_text)
        
        # Cleanup: Delete the uploaded file from Gemini
        try:
            genai.delete_file(uploaded_file.name)
            print("🗑️  Cleaned up Gemini file")
        except Exception:
            pass  # Ignore cleanup errors
        
        return result_json
        
    except json.JSONDecodeError as e:
        print(f"❌ JSON parsing error: {e}")
        print(f"Raw response: {result_text[:500]}...")
        return None
    except Exception as e:
        print(f"❌ Gemini API error: {e}")
        return None


def convert_gemini_response(gemini_data: dict) -> AnalysisResponse:
    """Convert Gemini's JSON response to our AnalysisResponse format."""
    questions = []
    
    for q in gemini_data.get("questions", []):
        # Map cognitive tag - handle SILLY_MISTAKE as FLUKE
        tag_str = q.get("cognitive_tag", "SOLID").upper()
        if tag_str == "SILLY_MISTAKE":
            tag_str = "FLUKE"
        try:
            cognitive_tag = CognitiveTag(tag_str)
        except ValueError:
            cognitive_tag = CognitiveTag.SOLID
        
        # Build interaction data
        interaction = InteractionData(
            ink_colors_used=q.get("ink_colors", ["blue"]),
            elimination_attempts=q.get("elimination_count", 0),
            strikethrough_count=q.get("strikethrough_count", 0)
        )
        
        # Build reasoning with ink evidence
        reasoning = q.get("reasoning", "Analysis pending")
        ink_evidence = q.get("ink_evidence", "")
        if ink_evidence and ink_evidence not in reasoning:
            reasoning = f"{reasoning} | Evidence: {ink_evidence}"
        
        # Build verdict
        verdict = AiVerdict(
            is_correct=q.get("is_correct", False),
            cognitive_tag=cognitive_tag,
            reasoning=reasoning,
            confidence_score=0.85
        )
        
        questions.append(QuestionResult(
            question_number=q.get("question_number", len(questions) + 1),
            interaction_data=interaction,
            ai_verdict=verdict
        ))
    
    # Calculate score if not provided
    score = gemini_data.get("score", 0.0)
    accuracy = gemini_data.get("accuracy", 0.0)
    
    if score == 0 and questions:
        correct = sum(1 for q in questions if q.ai_verdict.is_correct)
        wrong = len(questions) - correct
        score = (correct * 2) - (wrong * 0.66)
        accuracy = (correct / len(questions)) * 100 if questions else 0
    
    # Handle mentor_feedback - can be dict (structured) or string (legacy)
    raw_feedback = gemini_data.get("mentor_feedback", "")
    if isinstance(raw_feedback, dict):
        # Serialize structured feedback to JSON string for Android to parse
        mentor_feedback = json.dumps(raw_feedback)
    else:
        mentor_feedback = str(raw_feedback) if raw_feedback else ""
    
    return AnalysisResponse(
        test_subject=gemini_data.get("test_subject", "Unknown Subject"),
        total_questions=len(questions),
        analysis_timestamp=int(time.time() * 1000),
        questions=questions,
        mentor_feedback=mentor_feedback,
        score=round(score, 2),
        accuracy=round(accuracy, 1)
    )


# ==================== Mock Data Fallback ====================

def generate_mock_analysis() -> AnalysisResponse:
    """Fallback mock data when Gemini API is unavailable."""
    return AnalysisResponse(
        test_subject="Modern History - Indian National Movement (MOCK)",
        total_questions=10,
        analysis_timestamp=int(time.time() * 1000),
        score=12.7,  # Example: 8 correct (16) - 2 wrong (-1.32) = 14.68
        accuracy=80.0,
        mentor_feedback="⚠️ This is MOCK data. Configure GEMINI_API_KEY in .env for real analysis.\n\nOverall decent attempt! You have strong intuition but watch out for concept collapse areas.",
        questions=[
            QuestionResult(
                question_number=1,
                interaction_data=InteractionData(ink_colors_used=["blue"]),
                ai_verdict=AiVerdict(is_correct=True, cognitive_tag=CognitiveTag.SOLID, 
                    reasoning="Blue tick on Option C. Red strikethrough on Options A and B showing elimination. [MOCK]")
            ),
            QuestionResult(
                question_number=2,
                interaction_data=InteractionData(ink_colors_used=["blue", "red"], elimination_attempts=3),
                ai_verdict=AiVerdict(is_correct=False, cognitive_tag=CognitiveTag.CONCEPT_COLLAPSE,
                    reasoning="Heavy work visible: Red crosses on A, B, D. Circled Option C but wrong. Changed answer twice. [MOCK]")
            ),
            QuestionResult(
                question_number=3,
                interaction_data=InteractionData(ink_colors_used=["blue"]),
                ai_verdict=AiVerdict(is_correct=True, cognitive_tag=CognitiveTag.INTUITION,
                    reasoning="Single blue tick on Option B. No elimination marks. Quick, confident selection. [MOCK]")
            ),
            QuestionResult(
                question_number=4,
                interaction_data=InteractionData(ink_colors_used=["blue"]),
                ai_verdict=AiVerdict(is_correct=False, cognitive_tag=CognitiveTag.FLUKE,
                    reasoning="Hasty tick on Option A without any visible analysis. No eliminations attempted. [MOCK]")
            ),
            QuestionResult(
                question_number=5,
                interaction_data=InteractionData(ink_colors_used=["blue", "brown"]),
                ai_verdict=AiVerdict(is_correct=True, cognitive_tag=CognitiveTag.DOUBT,
                    reasoning="Brown question mark next to Options B and C. Final answer D is correct. Uncertainty visible. [MOCK]")
            ),
        ]
    )


# ==================== Endpoints ====================

@app.get("/")
async def root():
    """Health check endpoint."""
    return {
        "status": "ok",
        "service": "MockMate PDF Analyzer",
        "version": "2.1.0",
        "gemini_configured": bool(GEMINI_API_KEY and GEMINI_API_KEY != "your_key_here")
    }


@app.post("/analyze", response_model=AnalysisResponse)
async def analyze_pdf(file: UploadFile = File(...)):
    """
    Analyze an annotated PDF using Gemini 2.5 Flash.
    Falls back to mock data if API is unavailable.
    """
    # Validate file type
    if not file.content_type == "application/pdf":
        raise HTTPException(
            status_code=400,
            detail=f"Invalid file type: {file.content_type}. Expected application/pdf"
        )
    
    # Save file temporarily
    temp_path = TEMP_DIR / f"upload_{int(time.time() * 1000)}_{file.filename}"
    
    try:
        contents = await file.read()
        file_size_kb = len(contents) / 1024
        print(f"📄 Received PDF: {file.filename} ({file_size_kb:.1f} KB)")
        
        # Write to temp file
        with open(temp_path, "wb") as f:
            f.write(contents)
        
        # Try Gemini analysis
        gemini_result = await analyze_pdf_with_gemini(temp_path)
        
        if gemini_result:
            # Convert and return real analysis
            response = convert_gemini_response(gemini_result)
            print(f"✅ Real analysis complete: {response.total_questions} questions | Score: {response.score}")
            return response
        else:
            # Fall back to mock data
            print("⚠️  Using mock data fallback")
            return generate_mock_analysis()
            
    except Exception as e:
        print(f"❌ Error processing PDF: {e}")
        # Return mock data on any error
        return generate_mock_analysis()
        
    finally:
        # Cleanup temp file
        if temp_path.exists():
            temp_path.unlink()
            print("🗑️  Cleaned up temp file")


@app.get("/analyze/sample", response_model=AnalysisResponse)
async def get_sample_analysis():
    """Get sample analysis without uploading a file."""
    return generate_mock_analysis()


# ==================== Run Server ====================

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
