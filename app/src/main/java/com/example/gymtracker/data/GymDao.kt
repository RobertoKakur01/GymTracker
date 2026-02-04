package com.example.gymtracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Für Chart/History: genau die Spalten, die deine Query liefert


@Dao
interface GymDao {

    // -------- Library ----------
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun observeLibrary(): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Query("UPDATE exercises SET name = :newName WHERE id = :exerciseId")
    suspend fun renameExercise(exerciseId: Long, newName: String)

    @Query("SELECT COUNT(*) FROM day_exercises WHERE exerciseId = :exerciseId")
    suspend fun countUsage(exerciseId: Long): Int

    @Query("DELETE FROM exercises WHERE id = :exerciseId")
    suspend fun deleteExercise(exerciseId: Long)

    // -------- Days ----------
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun ensureDay(day: DayEntity)

    // -------- Active exercises per day ----------
    @Transaction
    @Query(
        """
        SELECT * FROM day_exercises
        WHERE dayDate = :dayDate
        ORDER BY id ASC
    """
    )
    fun observeActiveExercisesForDay(dayDate: String): Flow<List<DayExerciseWithData>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addExerciseToDay(dayExercise: DayExerciseEntity): Long

    @Query("DELETE FROM day_exercises WHERE id = :dayExerciseId")
    suspend fun removeExerciseFromDay(dayExerciseId: Long)

    // -------- Sets ----------
    @Insert
    suspend fun insertSet(set: SetEntity)

    @Query("DELETE FROM sets WHERE id = :setId")
    suspend fun deleteSet(setId: String)

    // -------- Exercise history for charts ----------
    // Liefert: dayDate, weightKg, reps, createdAt (genau wie deine Fehlermeldung)
    @Query(
        """
        SELECT de.dayDate AS dayDate,
               s.weightKg AS weightKg,
               s.reps     AS reps,
               s.createdAt AS createdAt
        FROM sets s
        INNER JOIN day_exercises de ON de.id = s.dayExerciseId
        WHERE de.exerciseId = :exerciseId
        ORDER BY de.dayDate ASC, s.createdAt ASC
    """
    )
    fun observeExerciseHistory(exerciseId: Long): Flow<List<ExerciseHistoryRow>>

    // -------- Templates ----------
    @Query("SELECT * FROM templates ORDER BY name ASC")
    fun observeTemplates(): Flow<List<TemplateEntity>>

    @Insert
    suspend fun insertTemplate(t: TemplateEntity): Long

    @Query("DELETE FROM templates WHERE id = :templateId")
    suspend fun deleteTemplate(templateId: Long)

    @Query(
        """
        SELECT e.* FROM exercises e
        INNER JOIN template_exercises te ON te.exerciseId = e.id
        WHERE te.templateId = :templateId
        ORDER BY te.sortOrder ASC, e.name ASC
    """
    )
    fun observeTemplateExercises(templateId: Long): Flow<List<ExerciseEntity>>

    @Query(
        """
        SELECT COALESCE(MAX(sortOrder), 0)
        FROM template_exercises
        WHERE templateId = :templateId
    """
    )
    suspend fun maxTemplateSort(templateId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addExerciseToTemplate(te: TemplateExerciseEntity)

    @Query("DELETE FROM template_exercises WHERE templateId = :templateId AND exerciseId = :exerciseId")
    suspend fun removeExerciseFromTemplate(templateId: Long, exerciseId: Long)

    @Query("SELECT exerciseId FROM template_exercises WHERE templateId = :templateId ORDER BY sortOrder ASC")
    suspend fun getTemplateExerciseIds(templateId: Long): List<Long>
}
