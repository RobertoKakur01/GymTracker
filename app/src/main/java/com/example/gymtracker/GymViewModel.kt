package com.example.gymtracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

// ---------- UI Models ----------
data class ExerciseSetUi(
    val id: String,
    val weightKg: Double,
    val reps: Int
)

data class ActiveExerciseUi(
    val dayExerciseId: Long,
    val exerciseId: Long,
    val name: String,
    val sets: List<ExerciseSetUi>
)


class GymViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = GymDatabase.get(app).dao()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    val templates = dao.observeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val selectedDateString: StateFlow<String> =
        selectedDate.map { it.toString() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, LocalDate.now().toString())

    // Bibliothek
    val libraryExercises = dao.observeLibrary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Aktive Übungen + Sets für ausgewählten Tag
    val activeExercisesForDay: StateFlow<List<ActiveExerciseUi>> =
        selectedDateString
            .flatMapLatest { day -> dao.observeActiveExercisesForDay(day) }
            .map { list ->
                list.map { item ->
                    ActiveExerciseUi(
                        dayExerciseId = item.dayExercise.id,
                        exerciseId = item.exercise.id,
                        name = item.exercise.name,
                        sets = item.sets
                            .sortedBy { it.createdAt }
                            .map { s -> ExerciseSetUi(s.id, s.weightKg, s.reps) }
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Übungen, die man für diesen Tag noch hinzufügen kann (Bibliothek minus aktive)
    val availableToAddForDay: StateFlow<List<ExerciseEntity>> =
        combine(libraryExercises, activeExercisesForDay) { lib, active ->
            val activeNames = active.map { it.name.lowercase() }.toSet()
            lib.filter { it.name.lowercase() !in activeNames }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- Date controls ----------
    fun prevDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun createTemplate(name: String, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) {
                onResult(false, "Name darf nicht leer sein."); return@launch
            }
            val id = dao.insertTemplate(TemplateEntity(name = trimmed))
            onResult(id > 0, if (id > 0) null else "Konnte nicht speichern.")
        }
    }

    fun observeTemplateExercises(templateId: Long): Flow<List<ExerciseEntity>> =
        dao.observeTemplateExercises(templateId)

    fun addExerciseToTemplate(templateId: Long, exerciseId: Long) {
        viewModelScope.launch {
            val next = dao.maxTemplateSort(templateId) + 1
            dao.addExerciseToTemplate(
                TemplateExerciseEntity(
                    templateId,
                    exerciseId,
                    sortOrder = next
                )
            )
        }
    }

    fun removeExerciseFromTemplate(templateId: Long, exerciseId: Long) {
        viewModelScope.launch { dao.removeExerciseFromTemplate(templateId, exerciseId) }
    }

    fun applyTemplateToSelectedDay(templateId: Long) {
        viewModelScope.launch {
            val day = selectedDateString.value
            dao.ensureDay(DayEntity(day))

            val ids = dao.getTemplateExerciseIds(templateId)

            // day_exercises hat bei dir onConflict=IGNORE → doppelte werden ignoriert
            ids.forEach { exId ->
                dao.addExerciseToDay(DayExerciseEntity(dayDate = day, exerciseId = exId))
            }
        }
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }

    // ---------- Library actions ----------
    fun addExerciseToLibrary(name: String, onResult: (success: Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) {
                onResult(false)
                return@launch
            }
            val id = dao.insertExercise(ExerciseEntity(name = trimmed))
            onResult(id != -1L)
        }
    }

    fun renameExercise(
        exerciseId: Long,
        newName: String,
        onResult: (success: Boolean, message: String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val trimmed = newName.trim()
            if (trimmed.isEmpty()) {
                onResult(false, "Name darf nicht leer sein.")
                return@launch
            }

            val exists = libraryExercises.value.any {
                it.name.equals(trimmed, ignoreCase = true) && it.id != exerciseId
            }
            if (exists) {
                onResult(false, "Diesen Namen gibt es schon in der Bibliothek.")
                return@launch
            }

            dao.renameExercise(exerciseId, trimmed)
            onResult(true, null)
        }
    }

    fun deleteExerciseFromLibrary(
        exerciseId: Long,
        onResult: (success: Boolean, message: String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val usage = dao.countUsage(exerciseId)
            if (usage > 0) {
                onResult(false, "Kann nicht löschen: Übung wird schon in Trainingsdaten verwendet.")
                return@launch
            }
            dao.deleteExercise(exerciseId)
            onResult(true, null)
        }
    }

    // ---------- Day actions ----------
    fun addExerciseToSelectedDay(exerciseId: Long) {
        viewModelScope.launch {
            val day = selectedDateString.value
            dao.ensureDay(DayEntity(day))
            dao.addExerciseToDay(DayExerciseEntity(dayDate = day, exerciseId = exerciseId))
        }
    }

    fun removeExerciseFromDay(dayExerciseId: Long) {
        viewModelScope.launch {
            dao.removeExerciseFromDay(dayExerciseId) // CASCADE löscht Sets automatisch
        }
    }


    fun deleteTemplate(templateId: Long) {
        viewModelScope.launch {
            dao.deleteTemplate(templateId)
        }
    }


    fun addSet(dayExerciseId: Long, weightKg: Double, reps: Int) {
        viewModelScope.launch {
            dao.insertSet(
                SetEntity(
                    id = UUID.randomUUID().toString(),
                    dayExerciseId = dayExerciseId,
                    weightKg = weightKg,
                    reps = reps,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteSet(setId: String) {
        viewModelScope.launch {
            dao.deleteSet(setId)
        }
    }

    // ---------- Charts (3 Modi) ----------
    fun observeChart(exerciseId: Long, mode: ChartMode): Flow<ChartSeries> {
        val inputFmt = DateTimeFormatter.ISO_LOCAL_DATE      // "YYYY-MM-DD"
        val labelFmt = DateTimeFormatter.ofPattern("dd.MM")  // "24.01"

        return dao.observeExerciseHistory(exerciseId).map { rows ->
            when (mode) {
                ChartMode.WEIGHT_OVER_TIME -> {
                    // pro Tag: max Gewicht (Top Set)
                    val byDay = rows.groupBy { it.dayDate }.toSortedMap()
                    val days = byDay.keys.toList()
                    val labels = days.map { LocalDate.parse(it, inputFmt).format(labelFmt) }

                    val points = days.mapIndexed { idx, day ->
                        val maxWeight = byDay[day]!!.maxOf { it.weightKg }
                        XYPoint(x = idx.toFloat(), y = maxWeight.toFloat())
                    }
                    ChartSeries(mode, points, xLabels = labels)
                }

                ChartMode.REPS_OVER_TIME -> {
                    // pro Tag: Summe Wiederholungen
                    val byDay = rows.groupBy { it.dayDate }.toSortedMap()
                    val days = byDay.keys.toList()
                    val labels = days.map { LocalDate.parse(it, inputFmt).format(labelFmt) }

                    val points = days.mapIndexed { idx, day ->
                        val repsSum = byDay[day]!!.sumOf { it.reps }
                        XYPoint(x = idx.toFloat(), y = repsSum.toFloat())
                    }
                    ChartSeries(mode, points, xLabels = labels)
                }


            }

        }
    }
}

