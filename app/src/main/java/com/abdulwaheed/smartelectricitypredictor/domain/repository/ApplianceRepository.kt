package com.abdulwaheed.smartelectricitypredictor.domain.repository

import com.abdulwaheed.smartelectricitypredictor.domain.model.Appliance
import kotlinx.coroutines.flow.Flow

interface ApplianceRepository {

    suspend fun saveAppliance(uid: String, appliance: Appliance): Result<Unit>
    suspend fun deleteAppliance(uid: String, applianceId: String): Result<Unit>
    suspend fun syncAppliances(uid: String): Result<Unit>
    fun observeAppliances(uid: String) : Flow<List<Appliance>>
}
