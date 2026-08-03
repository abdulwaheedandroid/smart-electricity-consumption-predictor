package com.abdulwaheed.smartelectricitypredictor.domain.repository

import com.abdulwaheed.smartelectricitypredictor.domain.model.UserProfile

interface ProfileRepository {
    suspend fun getProfile(uid: String): Result<UserProfile?>
    suspend fun saveProfile(profile: UserProfile): Result<Unit>
    suspend fun deleteProfile(uid: String): Result<Unit>
}
