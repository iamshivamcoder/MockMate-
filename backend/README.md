# MockMate PDF Analyzer Backend

FastAPI server for the Metacognitive PDF Analyzer feature.

## Setup

```bash
# Create virtual environment
python -m venv venv

# Activate (Windows)
.\venv\Scripts\activate

# Activate (macOS/Linux)
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt
```

## Run Server

```bash
# Development (auto-reload)
uvicorn main:app --reload --host 0.0.0.0 --port 8000

# Or directly
python main.py
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Health check |
| POST | `/analyze` | Upload PDF for analysis |
| GET | `/analyze/sample` | Get sample response (for testing) |

## Test with cURL

```bash
# Health check
curl http://localhost:8000/

# Upload PDF
curl -X POST "http://localhost:8000/analyze" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@test.pdf"

# Get sample (no upload needed)
curl http://localhost:8000/analyze/sample
```

## Response Schema

```json
{
  "test_subject": "Modern History - Indian National Movement",
  "total_questions": 10,
  "analysis_timestamp": 1706729000000,
  "questions": [
    {
      "question_number": 1,
      "interaction_data": {
        "ink_colors_used": ["blue"],
        "elimination_attempts": 0,
        "time_spent_seconds": 45,
        "strikethrough_count": 0
      },
      "ai_verdict": {
        "is_correct": true,
        "cognitive_tag": "SOLID",
        "reasoning": "Clean, confident approach...",
        "confidence_score": 0.95
      }
    }
  ]
}
```

## Cognitive Tags

| Tag | Meaning |
|-----|---------|
| `SOLID` | Confident, correct answer |
| `CONCEPT_COLLAPSE` | High effort, wrong answer |
| `INTUITION` | Quick, correct answer |
| `FLUKE` | Quick, wrong answer (careless) |
| `DOUBT` | Uncertainty markers (brown ink) |

## Android Integration

In your Android app, configure Retrofit to point to this server:

```kotlin
// For emulator
const val BASE_URL = "http://10.0.2.2:8000/"

// For physical device on same network
const val BASE_URL = "http://192.168.x.x:8000/"
```
