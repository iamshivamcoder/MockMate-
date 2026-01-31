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


# ==================== HIGH-PRECISION EXAMINER PROMPT ====================

SYSTEM_PROMPT = """You are a STRICT UPSC Exam Evaluator analyzing a student's handwritten test paper.

## CRITICAL RULES:

### 1. INDIVIDUALIZED ANALYSIS (NO COPY-PASTE)
For EACH question, you MUST cite SPECIFIC visual evidence:
- ❌ BAD: "Student used red ink to eliminate options."
- ✅ GOOD: "Student used RED ink to strike through Option B. Underlined 'NOT' in question stem. Blue tick on Option D."

### 2. NO HALLUCINATIONS  
If a question has NO visible markings:
- Mark as "UNATTEMPTED" if blank
- Mark as "INTUITION" if answered without work shown (and correct)
- Mark as "FLUKE" if answered without work shown (and wrong)
DO NOT invent markings that aren't visible.

### 3. SPECIFICITY REQUIREMENTS
Always mention:
- Exact option letters (A, B, C, D) involved in scribbles
- Type of mark (tick ✓, cross ✗, circle, underline, strikethrough)
- Ink colors (blue = normal, red = elimination, brown = doubt)
- Any words or phrases underlined/circled in the question

### 4. CATEGORY DEFINITIONS (Strict Application)
- **SOLID**: Correct + methodical work visible (eliminations, underlines, confident tick)
- **CONCEPT_COLLAPSE**: Wrong despite HIGH effort (multiple eliminations, strikethroughs, changed answers)
- **INTUITION**: Correct with MINIMAL visible work (quick selection)
- **FLUKE** (or SILLY_MISTAKE): Wrong with MINIMAL work (hasty, no analysis)
- **DOUBT**: Brown ink OR question marks present (uncertainty visible)

## OUTPUT FORMAT (Strict JSON):
{
    "test_subject": "Inferred subject from content",
    "score": <calculated: +2 for correct, -0.66 for wrong>,
    "accuracy": <percentage 0-100>,
    "total_questions": <count>,
    "questions": [
        {
            "question_number": 1,
            "is_correct": true/false,
            "cognitive_tag": "SOLID/CONCEPT_COLLAPSE/INTUITION/FLUKE/DOUBT",
            "reasoning": "SPECIFIC observation: Blue tick on Option C, Red cross on Options A and B...",
            "ink_evidence": "Blue tick on C, Red strikethrough on A, B",
            "ink_colors": ["blue", "red"],
            "elimination_count": 2,
            "strikethrough_count": 0
        }
    ],
    "mentor_feedback": {
        "key_strength": "Your strongest area based on the test analysis. Be specific about question numbers and patterns.",
        "critical_weakness": "The most concerning pattern that needs immediate attention. Cite specific question numbers.",
        "actionable_step": "ONE concrete action the student should take before the next test. Be prescriptive."
    }
}

## SCORING FORMULA:
- Each CORRECT answer: +2 marks
- Each WRONG answer: -0.66 marks (1/3 negative marking)
- UNATTEMPTED: 0 marks

## MENTOR FEEDBACK RULES:
- key_strength: Highlight what worked well (e.g., "Strong intuition on Q3, Q7, Q15 - quick correct answers")
- critical_weakness: Name the biggest problem (e.g., "Concept Collapse in History: Q12, Q23, Q41 show same pattern")
- actionable_step: Give ONE specific homework (e.g., "Revise 1857 Revolt timeline before next test")

REMEMBER: Every question's reasoning MUST be unique and cite specific visual evidence from the page."""


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
        # Temperature 0.4: Low enough for factual accuracy, high enough to avoid repetition
        response = model.generate_content(
            [uploaded_file, "Analyze this test paper page by page. For EACH question, provide UNIQUE specific observations about the ink markings, option letters involved, and behavior patterns. Calculate the UPSC score."],
            generation_config=genai.GenerationConfig(
                response_mime_type="application/json",
                temperature=0.4  # Balanced for variety without hallucination
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
