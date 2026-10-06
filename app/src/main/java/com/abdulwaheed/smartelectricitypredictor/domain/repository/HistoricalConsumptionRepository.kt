package com.abdulwaheed.smartelectricitypredictor.domain.repository

import com.abdulwaheed.smartelectricitypredictor.domain.model.HistoricalConsumption
import kotlinx.coroutines.flow.Flow

interface HistoricalConsumptionRepository {

    suspend fun saveHistoricalConsumption(
        uid: String,
        consumption: HistoricalConsumption
    ): Result<Unit>

    suspend fun deleteHistoricalConsumption(
        uid: String,
        consumptionId: String
    ): Result<Unit>

    suspend fun syncHistoricalConsumption(
        uid: String
    ): Result<Unit>

    fun observeHistoricalConsumption(
        uid: String
    ): Flow<List<HistoricalConsumption>>
}