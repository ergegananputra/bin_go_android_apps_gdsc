package com.gdsc.bingo.services.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gdsc.bingo.services.database.manager.VersionControlDao
import com.gdsc.bingo.services.database.manager.VersionControlTable

@Database(
    entities = [
        VersionControlTable::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun versionControlDao(): VersionControlDao

    companion object {
        private const val DATABASE_NAME = "gdsc_bingo_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}