package com.abdulwaheed.smartelectricitypredictor.domain.repository

import com.abdulwaheed.smartelectricitypredictor.domain.model.Appliance

interface ApplianceRepository {
    suspend fun getAppliances(uid: String): Result<List<Appliance>>
    suspend fun saveAppliance(uid: String, appliance: Appliance): Result<Unit>
    suspend fun deleteAppliance(uid: String, applianceId: String): Result<Unit>
}
