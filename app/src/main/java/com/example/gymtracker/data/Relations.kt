package com.example.gymtracker.data

import androidx.room.Embedded
import androidx.room.Relation

data class DayExerciseWithData(
    @Embedded val dayExercise: DayExerciseEntity,

    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "dayExerciseId"
    )
    val sets: List<SetEntity>
)
