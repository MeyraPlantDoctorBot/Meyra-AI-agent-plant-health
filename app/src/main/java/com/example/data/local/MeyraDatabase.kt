package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FarmerProfileEntity::class,
        CropHealthRecordEntity::class,
        PostHarvestRecordEntity::class,
        TreatmentHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MeyraDatabase : RoomDatabase() {
    abstract fun meyraDao(): MeyraDao

    companion object {
        @Volatile
        private var INSTANCE: MeyraDatabase? = null

        fun getDatabase(context: Context): MeyraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeyraDatabase::class.java,
                    "meyra_dphc_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
