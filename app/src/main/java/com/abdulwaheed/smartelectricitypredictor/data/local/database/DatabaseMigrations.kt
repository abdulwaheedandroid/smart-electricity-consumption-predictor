package com.abdulwaheed.smartelectricitypredictor.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `historical_consumption` (
                `userId` TEXT NOT NULL,
                `id` TEXT NOT NULL,
                `month` INTEGER NOT NULL,
                `year` INTEGER NOT NULL,
                `unitsConsumed` REAL NOT NULL,
                `billAmount` REAL NOT NULL,
                `createdAt` INTEGER,
                `updatedAt` INTEGER,
                `isDeleted` INTEGER NOT NULL,
                `syncPending` INTEGER NOT NULL,
                PRIMARY KEY(`userId`, `id`)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS
            `index_historical_consumption_userId_month_year`
            ON `historical_consumption` (`userId`, `month`, `year`)
            """.trimIndent()
        )
    }
}