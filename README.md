# MockMate - UPSC Exam Preparation App

MockMate is an Android application designed to help students prepare for the UPSC examination
through mock tests, practice sessions, and performance analytics.

## Features

- Mock Tests: Take full-length and subject-specific mock tests
- Performance Analysis: Track your progress and identify areas for improvement
- Test History: Review past attempts and performance trends
- Custom Test Import: Import your own test data via JSON format

## JSON Test Data Format

### Standard Multiple Choice Questions

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

### Match the Column Questions

For "Match the Column" questions, the `questions` array should contain objects with the following structure:

```json
{
  "name": "History Match the Column Test",
  "difficulty": "MEDIUM",
  "timeLimit": 30,
  "negativeMarking": true,
  "negativeMarkingValue": 0.33,
  "questions": [
    {
      "text": "Match the following historical events with their years.",
      "type": "MATCH_THE_COLUMN",
      "leftColumn": ["Quit India Movement", "Jallianwala Bagh Massacre", "Formation of Indian National Congress", "Partition of Bengal"],
      "rightColumn": ["1942", "1919", "1885", "1905"],
      "answers": ["1", "2", "3", "4"],
      "explanation": "A detailed explanation of the historical timeline and significance of each event.",
      "subject": "History",
      "topic": "Modern India",
      "difficulty": "MEDIUM"
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
- `options` (for MCQ): Array of options (typically 4 options)
- `correctOptionIndex` (for MCQ): Index of the correct option (0-based)
- `type` (for Match the Column): Must be `"MATCH_THE_COLUMN"`
- `leftColumn` (for Match the Column): An array of strings for the left column items.
- `rightColumn` (for Match the Column): An array of strings for the right column items. The order should correspond to the `leftColumn`.
- `answers` (for Match the Column): An array of strings representing the correct mapping. For example, if the first item in `leftColumn` matches the first item in `rightColumn`, the first answer would be "1".
- `explanation`: Explanation for the answer
- `subject`: Subject category (Indian Polity, History, Geography, etc.)
- `topic`: Specific topic within the subject
- `difficulty`: Question difficulty (EASY, MEDIUM, HARD)


## How to Import Test Data

1.  From the Dashboard, tap "Import Data".
2.  Choose one of the import methods:

    **A) Import from File:**
    - Prepare a JSON file with the correct format (either MCQ or Match the Column).
    - Tap "Select JSON File".
    - Choose your prepared JSON file.
    - Tap "Import Test Data".

    **B) Import from Prompt (for MCQs):**
    - Tap "Import from Prompt".
    - Paste your MCQ JSON data into the text field.
    - Tap "Import".

    **C) Import Match the Column (from Prompt):**
    - Tap "Import Match the Column".
    - A new dialog will appear.
    - Paste your "Match the Column" JSON data into the text field.
    - Tap "Import".

3.  Your test will be available in the mock tests section after a successful import.

## Requirements

- Android 8.0 (API level 26) or higher
- Internet connection for updates and sync
- Storage permission for importing/exporting tests from files.
