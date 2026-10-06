package com.abdulwaheed.smartelectricitypredictor.di

import android.content.Context
import androidx.room.Room
import com.abdulwaheed.smartelectricitypredictor.data.local.dao.ApplianceDao
import com.abdulwaheed.smartelectricitypredictor.data.local.dao.HistoricalConsumptionDao
import com.abdulwaheed.smartelectricitypredictor.data.local.database.AppDatabase
import com.abdulwaheed.smartelectricitypredictor.data.local.database.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smart_electricity_predictor_database.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideApplianceDao(
        database: AppDatabase
    ): ApplianceDao {
        return database.applianceDao()
    }

    @Provides
    fun provideHistoricalConsumptionDao(
        database: AppDatabase
    ): HistoricalConsumptionDao {
        return database.historicalConsumptionDao()
    }
}