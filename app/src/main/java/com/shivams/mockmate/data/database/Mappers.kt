package com.shivams.mockmate.data.database

import com.shivams.mockmate.model.TestAttempt

fun TestAttemptEntity.asDomainObject(): TestAttempt {
    return TestAttempt(
        id = id,
        testId = testId,
        startTime = startTime,
        endTime = endTime,
        userAnswers = emptyMap(), // Note: UserAnswers are handled separately in the repository
        isCompleted = isCompleted,
        score = score,
        customName = customName
    )
}

fun TestAttempt.asEntity(): TestAttemptEntity {
    return TestAttemptEntity(
        id = id,
        testId = testId,
        startTime = startTime,
        endTime = endTime,
        isCompleted = isCompleted,
        score = score,
        customName = customName
    )
}
