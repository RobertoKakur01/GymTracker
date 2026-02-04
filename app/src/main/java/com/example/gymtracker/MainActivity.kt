package com.example.gymtracker

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymtracker.ui.theme.AppTheme
import com.example.gymtracker.ui.theme.GymTrackerTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var appTheme by remember { mutableStateOf(AppTheme.ENERGY_ORANGE_DARK) }

            GymTrackerTheme(appTheme = appTheme) {
                TodayScreen(
                    currentTheme = appTheme,
                    onThemeChange = { theme -> appTheme = theme }
                )
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun TodayScreen(
    vm: GymViewModel = viewModel(),
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    val context = LocalContext.current

    val realToday = LocalDate.now()
    val selectedDate by vm.selectedDate.collectAsStateWithLifecycle()
    val library by vm.libraryExercises.collectAsStateWithLifecycle()
    val active by vm.activeExercisesForDay.collectAsStateWithLifecycle()
    val available by vm.availableToAddForDay.collectAsStateWithLifecycle()
    val templates by vm.templates.collectAsStateWithLifecycle()

    // ---- Date header formatting ----
    val locale = Locale.GERMAN
    val dowShort = selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
    val monthLong = selectedDate.month.getDisplayName(TextStyle.FULL, locale)
    val dayShort =
        String.format(locale, "%02d.%02d.", selectedDate.dayOfMonth, selectedDate.monthValue)
    val topDateLine = "$dowShort, $dayShort"
    val subDateLine = "$monthLong ${selectedDate.year}"

    // ----- Theme Sheet -----
    var showThemeSheet by remember { mutableStateOf(false) }

    // Dialog: neue Übung zur Bibliothek
    var showAddToLibraryDialog by remember { mutableStateOf(false) }
    var newLibraryExerciseName by remember { mutableStateOf("") }
    var libraryError by remember { mutableStateOf<String?>(null) }

    // Dialog: Satz hinzufügen
    var showAddSetDialog by remember { mutableStateOf(false) }
    var selectedDayExerciseId by remember { mutableStateOf<Long?>(null) }
    var selectedExerciseName by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }

    // Long-Press Löschmodi
    var deleteModeExerciseId by remember { mutableStateOf<Long?>(null) }
    var deleteModeSetId by remember { mutableStateOf<String?>(null) }

    // Chart Dialog + Dropdown Mode
    var showChartDialog by remember { mutableStateOf(false) }
    var chartExerciseId by remember { mutableStateOf<Long?>(null) }
    var chartExerciseName by remember { mutableStateOf("") }
    var selectedChartMode by remember { mutableStateOf(ChartMode.WEIGHT_OVER_TIME) }

    // Edit/Delete Bibliothek Dialog
    var showEditLibraryDialog by remember { mutableStateOf(false) }
    var editLibraryId by remember { mutableStateOf<Long?>(null) }
    var editLibraryName by remember { mutableStateOf("") }
    var editLibraryError by remember { mutableStateOf<String?>(null) }

    // Vorlage erstellen Dialog
    var showCreateTemplateDialog by remember { mutableStateOf(false) }
    var newTemplateName by remember { mutableStateOf("") }
    var templateError by remember { mutableStateOf<String?>(null) }

    // Template Editor Dialog
    var showTemplateEditorDialog by remember { mutableStateOf(false) }
    var editTemplateId by remember { mutableStateOf<Long?>(null) }
    var editTemplateName by remember { mutableStateOf("") }

    fun resetDeleteModes() {
        deleteModeExerciseId = null
        deleteModeSetId = null
    }

    fun openDatePicker() {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                vm.setDate(LocalDate.of(y, m + 1, d))
                resetDeleteModes()
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = topDateLine,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Text(
                            text = subDateLine,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { vm.prevDay(); resetDeleteModes() }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Vorheriger Tag")
                    }
                },
                actions = {
                    AssistChip(
                        onClick = { vm.setDate(realToday); resetDeleteModes() },
                        enabled = selectedDate != realToday,
                        label = { Text("Heute") }
                    )

                    IconButton(onClick = { showThemeSheet = true }) {
                        Text("🎨")
                    }

                    val canGoNext = selectedDate.isBefore(realToday)
                    IconButton(
                        onClick = { vm.nextDay(); resetDeleteModes() },
                        enabled = canGoNext
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Nächster Tag")
                    }

                    IconButton(onClick = { openDatePicker() }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Datum wählen")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                newLibraryExerciseName = ""
                libraryError = null
                showAddToLibraryDialog = true
            }) { Text("+") }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ---------------------------
            // Training (aktive Übungen)
            // ---------------------------
            if (active.isEmpty()) {
                item { Text("Noch keine Übungen aktiv. Wähle unten welche aus.") }
            } else {
                items(active) { ex ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    selectedDayExerciseId = ex.dayExerciseId
                                    selectedExerciseName = ex.name
                                    weightInput = ""
                                    repsInput = ""
                                    showAddSetDialog = true
                                },
                                onLongClick = {
                                    deleteModeExerciseId = ex.dayExerciseId
                                    deleteModeSetId = null
                                }
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(ex.name, style = MaterialTheme.typography.titleSmall)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AssistChip(
                                        onClick = {
                                            chartExerciseId = ex.exerciseId
                                            chartExerciseName = ex.name
                                            selectedChartMode = ChartMode.WEIGHT_OVER_TIME
                                            showChartDialog = true
                                            resetDeleteModes()
                                        },
                                        label = { Text("Graph") }
                                    )

                                    if (deleteModeExerciseId == ex.dayExerciseId) {
                                        IconButton(onClick = {
                                            vm.removeExerciseFromDay(ex.dayExerciseId)
                                            resetDeleteModes()
                                        }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Übung entfernen"
                                            )
                                        }
                                    }
                                }
                            }

                            if (ex.sets.isEmpty()) {
                                Text("Keine Sätze", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                Text("Sätze:", style = MaterialTheme.typography.labelLarge)

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ex.sets.forEach { set ->
                                        Surface(
                                            tonalElevation = 1.dp,
                                            shape = MaterialTheme.shapes.medium,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = {
                                                        if (deleteModeSetId == set.id) deleteModeSetId =
                                                            null
                                                    },
                                                    onLongClick = {
                                                        deleteModeSetId = set.id
                                                        deleteModeExerciseId = null
                                                    }
                                                )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 6.dp
                                                ),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("${set.weightKg} kg × ${set.reps}")

                                                if (deleteModeSetId == set.id) {
                                                    IconButton(onClick = {
                                                        vm.deleteSet(set.id)
                                                        deleteModeSetId = null
                                                    }) {
                                                        Icon(
                                                            Icons.Default.Close,
                                                            contentDescription = "Satz löschen"
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Text(
                                "Tippen: +Satz • Long-Press: Löschen",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(6.dp))
                HorizontalDivider()
                Spacer(Modifier.height(6.dp))
            }

            // ---------------------------
            // Vorlagen
            // ---------------------------
            item { Text("Vorlagen", style = MaterialTheme.typography.titleMedium) }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // + Vorlage erstellen
                    OutlinedCard(
                        modifier = Modifier.wrapContentWidth(),
                        onClick = {
                            newTemplateName = ""
                            templateError = null
                            showCreateTemplateDialog = true
                        }
                    ) {
                        Text(
                            "+ Vorlage",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }

                    // vorhandene Vorlagen
                    templates.forEach { t ->
                        OutlinedCard(
                            modifier = Modifier
                                .wrapContentWidth()
                                .combinedClickable(
                                    onClick = { vm.applyTemplateToSelectedDay(t.id) },
                                    onLongClick = {
                                        editTemplateId = t.id
                                        editTemplateName = t.name
                                        showTemplateEditorDialog = true
                                    }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(t.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(6.dp))
                HorizontalDivider()
                Spacer(Modifier.height(6.dp))
            }

            // ---------------------------
// Übung hinzufügen
// ---------------------------
            item { Text("Übung hinzufügen", style = MaterialTheme.typography.titleMedium) }

            if (available.isEmpty()) {
                item { Text("Alle Übungen sind für diesen Tag schon aktiv.") }
            } else {
                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        available.forEach { e ->
                            OutlinedCard(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .combinedClickable(
                                        onClick = {
                                            vm.addExerciseToSelectedDay(e.id)
                                            resetDeleteModes()
                                        },
                                        onLongClick = {
                                            editLibraryId = e.id
                                            editLibraryName = e.name
                                            editLibraryError = null
                                            showEditLibraryDialog = true
                                        }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 10.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = e.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }


            item { Spacer(Modifier.height(80.dp)) }
        }

        // ---------------------------
        // Theme Bottom Sheet (außerhalb LazyColumn)
        // ---------------------------
        if (showThemeSheet) {
            ModalBottomSheet(onDismissRequest = { showThemeSheet = false }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Theme auswählen", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))

                    AppTheme.entries.forEach { theme ->
                        ListItem(
                            headlineContent = { Text(theme.label) },
                            trailingContent = { if (theme == currentTheme) Text("✓") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeChange(theme)
                                    showThemeSheet = false
                                }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        // ---------------------------
        // Dialog: neue Übung
        // ---------------------------
        if (showAddToLibraryDialog) {
            AlertDialog(
                onDismissRequest = { showAddToLibraryDialog = false },
                title = { Text("Neue Übung") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newLibraryExerciseName,
                            onValueChange = {
                                newLibraryExerciseName = it
                                libraryError = null
                            },
                            label = { Text("Übungsname") },
                            singleLine = true,
                            isError = libraryError != null
                        )
                        if (libraryError != null) {
                            Text(libraryError!!, color = MaterialTheme.colorScheme.error)
                        }
                        Text(
                            "Aktuelle Anzahl der Übungen im Speicher: ${library.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val trimmed = newLibraryExerciseName.trim()
                        if (trimmed.isEmpty()) return@TextButton

                        vm.addExerciseToLibrary(trimmed) { success ->
                            if (!success) libraryError = "Gibt es schon in der Bibliothek."
                        }
                        if (libraryError == null) showAddToLibraryDialog = false
                    }) { Text("Speichern") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddToLibraryDialog = false }) { Text("Abbrechen") }
                }
            )
        }

        // ---------------------------
        // Dialog: Satz hinzufügen
        // ---------------------------
        if (showAddSetDialog && selectedDayExerciseId != null) {
            AlertDialog(
                onDismissRequest = { showAddSetDialog = false },
                title = { Text("Satz hinzufügen ($selectedExerciseName)") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Gewicht (kg)") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = repsInput,
                            onValueChange = { repsInput = it },
                            label = { Text("Wiederholungen") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val w = weightInput.replace(",", ".").toDoubleOrNull()
                        val r = repsInput.toIntOrNull()
                        val dayExId = selectedDayExerciseId
                        if (w != null && r != null && dayExId != null) vm.addSet(dayExId, w, r)
                        showAddSetDialog = false
                    }) { Text("Speichern") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddSetDialog = false }) { Text("Abbrechen") }
                }
            )
        }

        // ---------------------------
        // Dialog: Chart
        // ---------------------------
        if (showChartDialog && chartExerciseId != null) {
            var expanded by remember { mutableStateOf(false) }

            val series by vm.observeChart(chartExerciseId!!, selectedChartMode)
                .collectAsStateWithLifecycle(
                    initialValue = ChartSeries(
                        selectedChartMode,
                        emptyList()
                    )
                )

            AlertDialog(
                onDismissRequest = { showChartDialog = false },
                title = { Text("Verlauf: $chartExerciseName") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedChartMode.label,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Graph auswählen") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                ChartMode.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode.label) },
                                        onClick = {
                                            selectedChartMode = mode
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (series.points.size < 2) {
                            Text("Noch nicht genug Daten für einen Graphen.")
                        } else {
                            LineChart(points = series.points, xLabels = series.xLabels)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showChartDialog = false }) { Text("OK") }
                }
            )
        }

        // ---------------------------
        // Dialog: Vorlage erstellen
        // ---------------------------
        if (showCreateTemplateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateTemplateDialog = false },
                title = { Text("Neue Vorlage") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newTemplateName,
                            onValueChange = {
                                newTemplateName = it
                                templateError = null
                            },
                            label = { Text("Name der Vorlage (z.B. Push)") },
                            singleLine = true,
                            isError = templateError != null
                        )
                        if (templateError != null) {
                            Text(templateError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        vm.createTemplate(newTemplateName) { success, msg ->
                            if (!success) templateError = msg ?: "Fehler"
                            else showCreateTemplateDialog = false
                        }
                    }) { Text("Speichern") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateTemplateDialog = false }) { Text("Abbrechen") }
                }
            )
        }

        // ---------------------------
        // Dialog: Template Editor
        // ---------------------------
        if (showTemplateEditorDialog && editTemplateId != null) {
            val templateId = editTemplateId!!

            val templateExercises by vm.observeTemplateExercises(templateId)
                .collectAsStateWithLifecycle(initialValue = emptyList())

            val templateExerciseIds =
                remember(templateExercises) { templateExercises.map { it.id }.toSet() }
            val addableToTemplate = remember(library, templateExerciseIds) {
                library.filter { it.id !in templateExerciseIds }
            }

            AlertDialog(
                onDismissRequest = { showTemplateEditorDialog = false },
                title = { Text("Vorlage bearbeiten: $editTemplateName") },
                text = {
                    val scrollState = rememberScrollState()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 520.dp)
                    ) {
                        // Scrollbarer Inhalt (Android zeigt beim Scrollen automatisch eine Scrollbar)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .padding(end = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            Text(
                                "Übungen in der Vorlage",
                                style = MaterialTheme.typography.titleSmall
                            )

                            if (templateExercises.isEmpty()) {
                                Text("Noch keine Übungen in dieser Vorlage.")
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    templateExercises.forEach { ex ->
                                        OutlinedCard(modifier = Modifier.wrapContentWidth()) {
                                            Row(
                                                modifier = Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 10.dp
                                                ),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    ex.name,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                IconButton(onClick = {
                                                    vm.removeExerciseFromTemplate(
                                                        templateId,
                                                        ex.id
                                                    )
                                                }) {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Entfernen"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider()

                            Text("Übung hinzufügen", style = MaterialTheme.typography.titleSmall)

                            if (addableToTemplate.isEmpty()) {
                                Text("Keine weiteren Übungen verfügbar.")
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    addableToTemplate.forEach { ex ->
                                        OutlinedCard(
                                            modifier = Modifier.wrapContentWidth(),
                                            onClick = {
                                                vm.addExerciseToTemplate(
                                                    templateId,
                                                    ex.id
                                                )
                                            }
                                        ) {
                                            Text(
                                                text = ex.name,
                                                modifier = Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 10.dp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider()

                            TextButton(onClick = { vm.applyTemplateToSelectedDay(templateId) }) {
                                Text("Auf ausgewählten Tag anwenden")
                            }

                            Spacer(Modifier.height(12.dp))
                        }

                        // ✅ Scroll-Hint: Fade unten (zeigt “da ist mehr”)
                        val showFade = scrollState.value < scrollState.maxValue
                        if (showFade) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                            ),
                                            startY = 0f,
                                            endY = 80f
                                        )
                                    )
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTemplateEditorDialog = false }) { Text("Fertig") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        vm.deleteTemplate(templateId)
                        showTemplateEditorDialog = false
                    }) { Text("Vorlage löschen") }
                }
            )
        }

        // ---------------------------
        // Dialog: Bibliothek bearbeiten/löschen
        // ---------------------------
        if (showEditLibraryDialog && editLibraryId != null) {
            AlertDialog(
                onDismissRequest = { showEditLibraryDialog = false },
                title = { Text("Übung bearbeiten") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editLibraryName,
                            onValueChange = {
                                editLibraryName = it
                                editLibraryError = null
                            },
                            label = { Text("Name") },
                            singleLine = true,
                            isError = editLibraryError != null
                        )
                        if (editLibraryError != null) {
                            Text(editLibraryError!!, color = MaterialTheme.colorScheme.error)
                        }
                        Text(
                            "Umbenennen ändert den Namen überall (ID-Verknüpfung).",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        vm.renameExercise(editLibraryId!!, editLibraryName) { success, msg ->
                            if (!success) editLibraryError = msg
                            else showEditLibraryDialog = false
                        }
                    }) { Text("Speichern") }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            vm.deleteExerciseFromLibrary(editLibraryId!!) { success, msg ->
                                if (!success) editLibraryError = msg
                                else showEditLibraryDialog = false
                            }
                        }) { Text("Löschen") }

                        TextButton(onClick = {
                            showEditLibraryDialog = false
                        }) { Text("Abbrechen") }
                    }
                }
            )
        }
    }
}
