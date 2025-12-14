package com.shivams.mockmate.data

import com.shivams.mockmate.model.MockTest
import com.shivams.mockmate.model.Question
import com.shivams.mockmate.model.QuestionDifficulty
import com.shivams.mockmate.model.QuestionType
import com.shivams.mockmate.model.TestDifficulty
import java.util.UUID

fun generateSampleQuestions(): List<Question> {
    return listOf(
        Question(
            id = UUID.randomUUID().toString(),
            text = "Which article of the Indian Constitution deals with the Right to Equality?",
            options = listOf(
                "Article 14",
                "Article 19",
                "Article 21",
                "Article 25"
            ),
            correctOptionIndex = 0,
            explanation = "Article 14 guarantees equality before the law and equal protection of laws to all citizens.",
            difficulty = QuestionDifficulty.MEDIUM,
            type = QuestionType.MULTIPLE_CHOICE,
            subject = "Polity",
            topic = "Constitutional Provisions"
        ),
        Question(
            id = UUID.randomUUID().toString(),
            text = "Who is the current Chief Justice of India?",
            options = listOf(
                "Justice D.Y. Chandrachud",
                "Justice Sharad Arvind Bobde",
                "Justice Ranjan Gogoi",
                "Justice N.V. Ramana"
            ),
            correctOptionIndex = 0,
            explanation = "Justice D.Y. Chandrachud is the 50th Chief Justice of India, serving since November 2022.",
            difficulty = QuestionDifficulty.MEDIUM,
            type = QuestionType.MULTIPLE_CHOICE,
            subject = "Polity",
            topic = "Judiciary"
        ),
        Question(
            id = UUID.randomUUID().toString(),
            text = "Which of the following is NOT a fundamental right in India?",
            options = listOf(
                "Right to Equality",
                "Right to Freedom",
                "Right to Property",
                "Right to Education"
            ),
            correctOptionIndex = 2,
            explanation = "Right to Property was removed from the list of fundamental rights by the 44th Constitutional Amendment Act, 1978. It is now a legal right under Article 300A.",
            difficulty = QuestionDifficulty.HARD,
            type = QuestionType.MULTIPLE_CHOICE,
            subject = "Polity",
            topic = "Fundamental Rights"
        ),
        Question(
            id = UUID.randomUUID().toString(),
            text = "The Indian Constitution came into force on:",
            options = listOf(
                "January 26, 1950",
                "August 15, 1947",
                "November 26, 1949",
                "October 2, 1950"
            ),
            correctOptionIndex = 0,
            explanation = "The Constitution of India was adopted on November 26, 1949, and came into effect on January 26, 1950.",
            difficulty = QuestionDifficulty.EASY,
            type = QuestionType.MULTIPLE_CHOICE,
            subject = "Polity",
            topic = "Constitutional History"
        ),
        Question(
            id = UUID.randomUUID().toString(),
            text = "Which article of the Constitution abolishes untouchability?",
            options = listOf(
                "Article 14",
                "Article 17",
                "Article 21",
                "Article 25"
            ),
            correctOptionIndex = 1,
            explanation = "Article 17 of the Indian Constitution abolishes untouchability and forbids its practice in any form.",
            difficulty = QuestionDifficulty.MEDIUM,
            type = QuestionType.MULTIPLE_CHOICE,
            subject = "Polity",
            topic = "Fundamental Rights"
        ),
        Question(
            id = UUID.randomUUID().toString(),
            text = "What is the capital of Australia?",
            options = listOf(
                "Sydney",
                "Melbourne",
                "Canberra",
                "Perth"
            ),
            correctOptionIndex = 2,
            explanation = "Canberra is the capital city of Australia. It was specifically built to serve as the capital.",
            difficulty = QuestionDifficulty.EASY,
            type = QuestionType.MULTIPLE_CHOICE,
            subject = "Geography",
            topic = "World Geography"
        ),
        Question(
            id = UUID.randomUUID().toString(),
            text = "The highest peak in India is:",
            options = listOf(
                "Mount Everest",
                "Nanda Devi",
                "Kangchenjunga",
                "K2"
            ),
            correctOptionIndex = 2,
            explanation = "Kangchenjunga (8,586 m) is the highest peak in India and the third highest in the world.",
            difficulty = QuestionDifficulty.MEDIUM,
            type = QuestionType.MULTIPLE_CHOICE,
            subject = "Geography",
            topic = "Indian Geography"
        ),
        Question(
            id = UUID.randomUUID().toString(),
            text = "Which river is known as the 'Sorrow of Bihar'?",
            options = listOf(
                "Ganga",
                "Yamuna",
                "Kosi",
                "Ghaghara"
            ),
            correctOptionIndex = 2,
            explanation = "The Kosi river is known as the 'Sorrow of Bihar' due to frequent flooding and changing course.",
            difficulty = QuestionDifficulty.HARD,
            type = QuestionType.MULTIPLE_CHOICE,
            subject = "Geography",
            topic = "Indian Geography"
        ),
        Question(
            id = UUID.randomUUID().toString(),
            text = "The deepest oceanic trench in the world is:",
            options = listOf(
                "Mariana Trench",
                "Java Trench",
                "Philippine Trench",
                "Puerto Rico Trench"
            ),
            correctOptionIndex = 0,
            explanation = "The Mariana Trench, located in the western Pacific Ocean, is the deepest oceanic trench with a depth of about 11,034 meters.",
            difficulty = QuestionDifficulty.MEDIUM,
            type = QuestionType.MULTIPLE_CHOICE,
            subject = "Geography",
            topic = "Oceanography"
        ),
        Question(
            id = UUID.randomUUID().toString(),
            text = "Which of the following is a cold ocean current?",
            options = listOf(
                "Gulf Stream",
                "Kuroshio Current",
                "Brazil Current",
                "Benguela Current"
            ),
            correctOptionIndex = 3,
            explanation = "The Benguela Current is a cold ocean current that flows northwards along the western coast of Southern Africa.",
            difficulty = QuestionDifficulty.HARD,
            type = QuestionType.MULTIPLE_CHOICE,
            subject = "Geography",
            topic = "Oceanography"
        )
    )
}

fun generateSampleTests(): List<MockTest> {
    val questions = generateSampleQuestions()
    val polityQuestions = questions.filter { it.subject == "Polity" }
    val geographyQuestions = questions.filter { it.subject == "Geography" }


    return listOf(
        MockTest(
            id = UUID.randomUUID().toString(),
            name = "Polity Master Test",
            difficulty = TestDifficulty.HARD,
            questions = polityQuestions,
            timeLimit = 30,
            negativeMarking = true,
            negativeMarkingValue = 0.25f
        ),
        MockTest(
            id = UUID.randomUUID().toString(),
            name = "Geography Challenge",
            difficulty = TestDifficulty.MEDIUM,
            questions = geographyQuestions,
            timeLimit = 25,
            negativeMarking = false
        ),
        MockTest(
            id = UUID.randomUUID().toString(),
            name = "Mixed Subject Exam",
            difficulty = TestDifficulty.MEDIUM,
            questions = questions,
            timeLimit = 45,
            negativeMarking = true,
            negativeMarkingValue = 0.33f
        ),
        MockTest(
        id = UUID.randomUUID().toString(),
        name = "Basic Knowledge Test",
        difficulty = TestDifficulty.EASY,
        questions = questions.filter { it.difficulty == QuestionDifficulty.EASY },
        timeLimit = 15,
        negativeMarking = false
        )
    )
}