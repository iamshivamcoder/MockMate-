# MockMate - UPSC Exam Preparation App

MockMate is an Android application designed to help students prepare for the UPSC examination
through mock tests, practice sessions, and performance analytics.

## Features

- Mock Tests: Take full-length and subject-specific mock tests
- Performance Analysis: Track your progress and identify areas for improvement
- Test History: Review past attempts and performance trends
- Custom Test Import: Import your own test data via JSON format

## JSON Test Data Format

To import custom test data, create a JSON file with the following structure:

```json
{
  "name": "UPSC Prelims 2023",
  "difficulty": "HARD",
  "timeLimit": 120,
  "negativeMarking": true,
  "negativeMarkingValue": 0.33,
  "questions": [
    {
      "text": "Which article deals with Right to Equality?",
      "options": ["Article 14", "Article 19", "Article 21", "Article 32"],
      "correctOptionIndex": 0,
      "explanation": "Article 14 provides equality before law.",
      "subject": "Indian Polity",
      "topic": "Constitution",
      "difficulty": "MEDIUM"
    },
    {
      "text": "Who was the first Governor-General of independent India?",
      "options": ["C. Rajagopalachari", "Lord Mountbatten", "Dr. Rajendra Prasad", "Lord Wavell"],
      "correctOptionIndex": 0,
      "explanation": "C. Rajagopalachari was the first Governor-General of independent India.",
      "subject": "History",
      "topic": "Modern India",
      "difficulty": "EASY"
    }

  ]
}
```

### Field Descriptions

- `name`: Name of the test
- `difficulty`: Difficulty level (EASY, MEDIUM, HARD)
- `timeLimit`: Time limit in minutes
- `negativeMarking`: Whether negative marking is enabled (true/false)
- `negativeMarkingValue`: Value for negative marking (e.g., 0.33 means 1/3rd mark deducted for wrong
  answer)

#### Question Fields

- `text`: The question text
- `options`: Array of options (typically 4 options)
- `correctOptionIndex`: Index of the correct option (0-based)
- `explanation`: Explanation for the answer
- `subject`: Subject category (Indian Polity, History, Geography, etc.)
- `topic`: Specific topic within the subject
- `difficulty`: Question difficulty (EASY, MEDIUM, HARD)

## How to Import Test Data

1. Create a JSON file following the format above
2. From the Dashboard, tap "Import Data"
3. Select your JSON file
4. Review and confirm the import
5. Your test will be available in the mock tests section

## Requirements

- Android 8.0 (API level 26) or higher
- Internet connection for updates and sync
- Storage permission for importing/exporting tests