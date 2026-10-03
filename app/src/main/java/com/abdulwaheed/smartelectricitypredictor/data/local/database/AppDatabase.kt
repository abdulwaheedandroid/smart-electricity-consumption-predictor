package com.abdulwaheed.smartelectricitypredictor.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.abdulwaheed.smartelectricitypredictor.data.local.dao.ApplianceDao
import com.abdulwaheed.smartelectricitypredictor.data.local.entity.ApplianceEntity


@Database(
    entities = [ApplianceEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun applianceDao(): ApplianceDao
}