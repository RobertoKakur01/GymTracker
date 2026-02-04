package com.example.gymtracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    indices = [Index(value = ["name"], unique = true)]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(tableName = "days")
data class DayEntity(
    @PrimaryKey val date: String // "YYYY-MM-DD"
)

@Entity(
    tableName = "day_exercises",
    foreignKeys = [
        ForeignKey(
            entity = DayEntity::class,
            parentColumns = ["date"],
            childColumns = ["dayDate"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("dayDate"),
        Index("exerciseId"),
        Index(value = ["dayDate", "exerciseId"], unique = true)
    ]
)
data class DayExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayDate: String,
    val exerciseId: Long
)

@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = DayExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["dayExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dayExerciseId")]
)
data class SetEntity(
    @PrimaryKey val id: String, // UUID
    val dayExerciseId: Long,
    val weightKg: Double,
    val reps: Int,
    val createdAt: Long
)
