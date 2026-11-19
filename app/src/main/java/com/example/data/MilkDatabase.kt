package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MilkEntryEntity::class,
        MonthRecordEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MilkDatabase : RoomDatabase() {
    abstract fun milkDao(): MilkDao

    companion object {
        @Volatile
        private var INSTANCE: MilkDatabase? = null

        fun getInstance(context: Context): MilkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MilkDatabase::class.java,
                    "doodh_hisab.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
