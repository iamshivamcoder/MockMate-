package com.shivams.mockmate.data.repositories

import com.shivams.mockmate.data.database.UserProfileDao
import com.shivams.mockmate.model.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserProfileRepository @Inject constructor(private val userProfileDao: UserProfileDao) {

    fun getUserProfile(): Flow<UserProfile?> = userProfileDao.getUserProfile()

    suspend fun saveUserProfile(userProfile: UserProfile) {
        userProfileDao.insertOrUpdate(userProfile)
    }
}
