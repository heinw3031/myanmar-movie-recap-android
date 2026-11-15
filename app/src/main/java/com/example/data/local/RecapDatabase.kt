package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RecapEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RecapDatabase : RoomDatabase() {
    abstract fun recapDao(): RecapDao

    companion object {
        @Volatile
        private var INSTANCE: RecapDatabase? = null

        fun getInstance(context: Context): RecapDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RecapDatabase::class.java,
                    "myanmar_movie_recap.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
