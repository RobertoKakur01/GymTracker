package com.example.gymtracker.data

data class ExerciseHistoryRow(
    val dayDate: String,
    val weightKg: Double,
    val reps: Int,
    val createdAt: Long
)
