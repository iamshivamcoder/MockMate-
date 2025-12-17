package com.shivams.mockmate.domain.usecases

import com.shivams.mockmate.data.repositories.TestRepository
import javax.inject.Inject

/**
 * Use case for common test attempt operations (delete, rename).
 * Centralizes logic to avoid duplication across ViewModels.
 */
class TestAttemptOperationsUseCase @Inject constructor(
    private val repository: TestRepository
) {
    /**
     * Deletes a test attempt by ID.
     * @param attemptId The ID of the test attempt to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteTestAttempt(attemptId: String): Result<Unit> {
        return try {
            repository.deleteTestAttempt(attemptId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Renames a test attempt with a custom name.
     * @param attemptId The ID of the test attempt to rename
     * @param customName The new custom name
     * @return Result indicating success or failure
     */
    suspend fun renameTestAttempt(attemptId: String, customName: String): Result<Unit> {
        return try {
            if (customName.isBlank()) {
                return Result.failure(IllegalArgumentException("Name cannot be empty"))
            }
            repository.updateTestAttemptCustomName(attemptId, customName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
