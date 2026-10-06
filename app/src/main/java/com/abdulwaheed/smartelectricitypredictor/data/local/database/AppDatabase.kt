package com.abdulwaheed.smartelectricitypredictor.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.abdulwaheed.smartelectricitypredictor.data.local.dao.ApplianceDao
import com.abdulwaheed.smartelectricitypredictor.data.local.dao.HistoricalConsumptionDao
import com.abdulwaheed.smartelectricitypredictor.data.local.entity.ApplianceEntity
import com.abdulwaheed.smartelectricitypredictor.data.local.entity.HistoricalConsumptionEntity

@Database(
    entities = [
        ApplianceEntity::class,
        HistoricalConsumptionEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun applianceDao(): ApplianceDao

    abstract fun historicalConsumptionDao(): HistoricalConsumptionDao
}