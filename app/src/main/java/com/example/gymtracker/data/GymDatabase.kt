package com.example.gymtracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ExerciseEntity::class,
        DayEntity::class,
        DayExerciseEntity::class,
        SetEntity::class,
        TemplateEntity::class,              // ✅ neu
        TemplateExerciseEntity::class       // ✅ neu
    ],
    version = 2, // ✅ hochzählen
    exportSchema = false
)
abstract class GymDatabase : RoomDatabase() {

    abstract fun dao(): GymDao


    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null

        fun get(context: Context): GymDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }

            }
    }
}
