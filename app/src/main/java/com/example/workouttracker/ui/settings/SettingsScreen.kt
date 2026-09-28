package com.example.workouttracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.model.Exercise
import com.example.workouttracker.model.MuscleCategory
import com.example.workouttracker.model.WorkoutDay
import com.example.workouttracker.model.WorkoutTimer
import com.example.workouttracker.ui.components.CategoryBadge
import com.example.workouttracker.ui.components.ConfirmDialog

@Composable
fun SettingsScreen(
    days: List<WorkoutDay>,
    exercises: List<Exercise>,
    categories: List<MuscleCategory>,
    timers: List<WorkoutTimer>,
    initialDayId: String?,
    onAddDay: (name: String, dayOfWeek: String) -> Unit,
    onUpdateDay: (id: String, name: String, dayOfWeek: String) -> Unit,
    onDeleteDay: (id: String) -> Unit,
    onReorderDays: (List<WorkoutDay>) -> Unit,
    onAddExercise: (
        dayId: String,
        name: String,
        categoryId: String,
        weight: Double?,
        weightUnit: String,
        setsCount: Int,
        workTimeSeconds: Int,
        restTimeSeconds: Int,
        timerId: String?
    ) -> Unit,
    onUpdateExercise: (Exercise) -> Unit,
    onDeleteExercise: (id: String) -> Unit,
    onReorderExercises: (dayId: String, List<Exercise>) -> Unit,
    onAddCustomCategory: (name: String, colorHex: Long) -> Unit,
    onDeleteCustomCategory: (id: String) -> Unit,
    onResetWeeklyMarks: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(if (initialDayId != null) 1 else 0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Settings Header
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
                text = "Ajustes",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Configuración completa de rutinas, días, series y marcas",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section Tabs
        val tabs = listOf("Días", "Rutina y Ejercicios", "Categorías", "Marcas Semanales")
        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        when (selectedSection) {
            0 -> DaysManagementSection(
                days = days,
                onAddDay = onAddDay,
                onUpdateDay = onUpdateDay,
                onDeleteDay = onDeleteDay,
                onReorderDays = onReorderDays
            )
            1 -> ExercisesManagementSection(
                days = days,
                exercises = exercises,
                categories = categories,
                timers = timers,
                initialDayId = initialDayId,
                onAddExercise = onAddExercise,
                onUpdateExercise = onUpdateExercise,
                onDeleteExercise = onDeleteExercise,
                onReorderExercises = onReorderExercises,
                onAddCustomCategory = onAddCustomCategory
            )
            2 -> CategoriesManagementSection(
                categories = categories,
                onAddCustomCategory = onAddCustomCategory,
                onDeleteCustomCategory = onDeleteCustomCategory
            )
            3 -> WeeklyMarksSection(
                onResetWeeklyMarks = onResetWeeklyMarks
            )
        }
    }
}

// ====================================================================
// 1. DAYS MANAGEMENT
// ====================================================================

@Composable
fun DaysManagementSection(
    days: List<WorkoutDay>,
    onAddDay: (name: String, dayOfWeek: String) -> Unit,
    onUpdateDay: (id: String, name: String, dayOfWeek: String) -> Unit,
    onDeleteDay: (id: String) -> Unit,
    onReorderDays: (List<WorkoutDay>) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var dayToEdit by remember { mutableStateOf<WorkoutDay?>(null) }
    var dayToDelete by remember { mutableStateOf<WorkoutDay?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Días de entrenamiento (${days.size})",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Agregar día", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (days.isEmpty()) {
            EmptySectionCard(
                icon = Icons.Default.CalendarToday,
                message = "No tienes días de entrenamiento creados.",
                buttonText = "Crear primer día",
                onClick = { showAddDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(days, key = { it.id }) { day ->
                    val index = days.indexOf(day)
                    DayCard(
                        day = day,
                        canMoveUp = index > 0,
                        canMoveDown = index < days.size - 1,
                        onMoveUp = {
                            val mutable = days.toMutableList()
                            val temp = mutable[index]
                            mutable[index] = mutable[index - 1]
                            mutable[index - 1] = temp
                            onReorderDays(mutable)
                        },
                        onMoveDown = {
                            val mutable = days.toMutableList()
                            val temp = mutable[index]
                            mutable[index] = mutable[index + 1]
                            mutable[index + 1] = temp
                            onReorderDays(mutable)
                        },
                        onEdit = { dayToEdit = day },
                        onDelete = { dayToDelete = day }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        DayEditDialog(
            title = "Agregar Día de Entrenamiento",
            initialName = "",
            initialDayOfWeek = "Lunes",
            onConfirm = { name, dow ->
                onAddDay(name, dow)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    dayToEdit?.let { day ->
        DayEditDialog(
            title = "Editar Día",
            initialName = day.name,
            initialDayOfWeek = day.dayOfWeek,
            onConfirm = { name, dow ->
                onUpdateDay(day.id, name, dow)
                dayToEdit = null
            },
            onDismiss = { dayToEdit = null }
        )
    }

    dayToDelete?.let { day ->
        ConfirmDialog(
            title = "Eliminar Día",
            message = "¿Deseas eliminar \"${day.name}\"? Se eliminarán también los ejercicios configurados para este día.",
            confirmText = "Eliminar",
            isDestructive = true,
            onConfirm = {
                onDeleteDay(day.id)
                dayToDelete = null
            },
            onDismiss = { dayToDelete = null }
        )
    }
}

@Composable
fun DayCard(
    day: WorkoutDay,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = day.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Día base: ${day.dayOfWeek}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Reorder arrows
            IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.ArrowUpward,
                    contentDescription = "Mover arriba",
                    tint = if (canMoveUp) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.ArrowDownward,
                    contentDescription = "Mover abajo",
                    tint = if (canMoveDown) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEditDialog(
    title: String,
    initialName: String,
    initialDayOfWeek: String,
    onConfirm: (name: String, dayOfWeek: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var dayOfWeek by remember { mutableStateOf(initialDayOfWeek) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del día") },
                    placeholder = { Text("Ej. Lunes - Pecho & Tríceps") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = dayOfWeek,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Día de la semana") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        WorkoutDay.DAYS_OF_WEEK.forEach { dow ->
                            DropdownMenuItem(
                                text = { Text(dow) },
                                onClick = {
                                    dayOfWeek = dow
                                    if (name.isEmpty()) name = dow
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = name.trim().ifEmpty { dayOfWeek }
                    onConfirm(finalName, dayOfWeek)
                }
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// ====================================================================
// 2. EXERCISES & ROUTINE MANAGEMENT
// ====================================================================

@Composable
fun ExercisesManagementSection(
    days: List<WorkoutDay>,
    exercises: List<Exercise>,
    categories: List<MuscleCategory>,
    timers: List<WorkoutTimer>,
    initialDayId: String?,
    onAddExercise: (
        dayId: String,
        name: String,
        categoryId: String,
        weight: Double?,
        weightUnit: String,
        setsCount: Int,
        workTimeSeconds: Int,
        restTimeSeconds: Int,
        timerId: String?
    ) -> Unit,
    onUpdateExercise: (Exercise) -> Unit,
    onDeleteExercise: (id: String) -> Unit,
    onReorderExercises: (dayId: String, List<Exercise>) -> Unit,
    onAddCustomCategory: (name: String, colorHex: Long) -> Unit
) {
    var selectedDayId by remember {
        mutableStateOf(
            if (initialDayId != null && days.any { it.id == initialDayId }) initialDayId
            else days.firstOrNull()?.id
        )
    }

    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }

    if (days.isEmpty()) {
        EmptySectionCard(
            icon = Icons.Default.CalendarToday,
            message = "Primero debes crear al menos un día de entrenamiento en la pestaña 'Días'.",
            buttonText = "No hay días creados",
            onClick = {}
        )
        return
    }

    val currentDayId = selectedDayId ?: days.first().id
    val currentDayExercises = exercises.filter { it.dayId == currentDayId }.sortedBy { it.order }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Horizontal day selector chips
        Text(
            text = "Selecciona el día para gestionar sus ejercicios:",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEach { day ->
                val isSelected = day.id == currentDayId
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedDayId = day.id },
                    label = { Text(day.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Header for day exercises + Add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ejercicios (${currentDayExercises.size})",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Button(
                onClick = { showAddExerciseDialog = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Agregar ejercicio", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (currentDayExercises.isEmpty()) {
            EmptySectionCard(
                icon = Icons.Default.FitnessCenter,
                message = "Este día no tiene ejercicios asignados todavía.",
                buttonText = "Agregar primer ejercicio",
                onClick = { showAddExerciseDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(currentDayExercises, key = { it.id }) { exercise ->
                    val index = currentDayExercises.indexOf(exercise)
                    val cat = categories.find { it.id == exercise.categoryId }
                        ?: MuscleCategory.DEFAULT_CATEGORIES.first()

                    ExerciseSettingCard(
                        exercise = exercise,
                        category = cat,
                        canMoveUp = index > 0,
                        canMoveDown = index < currentDayExercises.size - 1,
                        onMoveUp = {
                            val mutable = currentDayExercises.toMutableList()
                            val temp = mutable[index]
                            mutable[index] = mutable[index - 1]
                            mutable[index - 1] = temp
                            onReorderExercises(currentDayId, mutable)
                        },
                        onMoveDown = {
                            val mutable = currentDayExercises.toMutableList()
                            val temp = mutable[index]
                            mutable[index] = mutable[index + 1]
                            mutable[index + 1] = temp
                            onReorderExercises(currentDayId, mutable)
                        },
                        onEdit = { exerciseToEdit = exercise },
                        onDelete = { exerciseToDelete = exercise }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Add Exercise Dialog
    if (showAddExerciseDialog) {
        ExerciseEditDialog(
            title = "Nuevo Ejercicio",
            exercise = null,
            dayId = currentDayId,
            categories = categories,
            timers = timers,
            onConfirm = { name, catId, weight, unit, sets, workTime, restTime, timerId ->
                onAddExercise(currentDayId, name, catId, weight, unit, sets, workTime, restTime, timerId)
                showAddExerciseDialog = false
            },
            onAddCustomCategory = onAddCustomCategory,
            onDismiss = { showAddExerciseDialog = false }
        )
    }

    // Edit Exercise Dialog
    exerciseToEdit?.let { exercise ->
        ExerciseEditDialog(
            title = "Editar Ejercicio",
            exercise = exercise,
            dayId = exercise.dayId,
            categories = categories,
            timers = timers,
            onConfirm = { name, catId, weight, unit, sets, workTime, restTime, timerId ->
                onUpdateExercise(
                    exercise.copy(
                        name = name,
                        categoryId = catId,
                        weight = weight,
                        weightUnit = unit,
                        setsCount = sets,
                        workTimeSeconds = workTime,
                        restTimeSeconds = restTime,
                        timerId = timerId
                    )
                )
                exerciseToEdit = null
            },
            onAddCustomCategory = onAddCustomCategory,
            onDismiss = { exerciseToEdit = null }
        )
    }

    // Delete Exercise Dialog
    exerciseToDelete?.let { exercise ->
        ConfirmDialog(
            title = "Eliminar Ejercicio",
            message = "¿Deseas eliminar permanentemente \"${exercise.name}\"?",
            confirmText = "Eliminar",
            isDestructive = true,
            onConfirm = {
                onDeleteExercise(exercise.id)
                exerciseToDelete = null
            },
            onDismiss = { exerciseToDelete = null }
        )
    }
}

@Composable
fun ExerciseSettingCard(
    exercise: Exercise,
    category: MuscleCategory,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = exercise.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    CategoryBadge(category = category)
                }

                Spacer(modifier = Modifier.height(4.dp))

                val details = buildString {
                    append("${exercise.setsCount} series")
                    if (exercise.weight != null && exercise.weight > 0) {
                        append(" • ${exercise.weight} ${exercise.weightUnit}")
                    }
                    if (exercise.restTimeSeconds > 0) {
                        append(" • Descanso: ${exercise.restTimeSeconds}s")
                    } else {
                        append(" • Sin descanso")
                    }
                }

                Text(
                    text = details,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Reorder
            IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(30.dp)) {
                Icon(
                    Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (canMoveUp) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(30.dp)) {
                Icon(
                    Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (canMoveDown) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseEditDialog(
    title: String,
    exercise: Exercise?,
    dayId: String,
    categories: List<MuscleCategory>,
    timers: List<WorkoutTimer>,
    onConfirm: (
        name: String,
        categoryId: String,
        weight: Double?,
        weightUnit: String,
        setsCount: Int,
        workTimeSeconds: Int,
        restTimeSeconds: Int,
        timerId: String?
    ) -> Unit,
    onAddCustomCategory: (name: String, colorHex: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(exercise?.name ?: "") }
    var selectedCategoryId by remember { mutableStateOf(exercise?.categoryId ?: categories.first().id) }
    var weightText by remember { mutableStateOf(exercise?.weight?.toString() ?: "") }
    var weightUnit by remember { mutableStateOf(exercise?.weightUnit ?: "kg") }
    var setsCountText by remember { mutableStateOf((exercise?.setsCount ?: 3).toString()) }
    var restTimeText by remember { mutableStateOf((exercise?.restTimeSeconds ?: 90).toString()) }
    var selectedTimerId by remember { mutableStateOf(exercise?.timerId) }
    var isError by remember { mutableStateOf(false) }

    var showCreateCustomCategoryDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del ejercicio") },
                    placeholder = { Text("Ej. Press banca, Sentadilla...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selection
                Text(
                    text = "Categoría Muscular:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat.id == selectedCategoryId
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedCategoryId = cat.id }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) cat.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            color = if (isSelected) cat.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(cat.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Add Custom Category Chip
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showCreateCustomCategoryDialog = true }
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Nueva categoría", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Weight & Sets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Peso (opcional)") },
                        placeholder = { Text("Ej. 60") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )

                    OutlinedTextField(
                        value = setsCountText,
                        onValueChange = { setsCountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Series") },
                        placeholder = { Text("3") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Rest Time
                Text(
                    text = "Tiempo de descanso tras la serie:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = restTimeText,
                    onValueChange = { restTimeText = it.filter { c -> c.isDigit() } },
                    label = { Text("Descanso (segundos)") },
                    placeholder = { Text("90") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick preset row for rest
                Text(
                    text = "Preajustes rápidos de descanso:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(30, 45, 60, 90, 120, 180).forEach { sec ->
                        OutlinedButton(
                            onClick = { restTimeText = sec.toString() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                        ) {
                            Text("${sec}s", fontSize = 10.sp)
                        }
                    }
                }

                if (isError) {
                    Text(
                        text = "El nombre del ejercicio es obligatorio",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isNotEmpty()) {
                        val weight = weightText.toDoubleOrNull()
                        val sets = (setsCountText.toIntOrNull() ?: 3).coerceAtLeast(1)
                        val restSec = restTimeText.toIntOrNull() ?: 0

                        onConfirm(
                            name.trim(),
                            selectedCategoryId,
                            weight,
                            weightUnit,
                            sets,
                            0, // workTimeSeconds removed, only restTimeSeconds
                            restSec,
                            selectedTimerId
                        )
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (showCreateCustomCategoryDialog) {
        CustomCategoryDialog(
            onConfirm = { catName, hex ->
                onAddCustomCategory(catName, hex)
                showCreateCustomCategoryDialog = false
            },
            onDismiss = { showCreateCustomCategoryDialog = false }
        )
    }
}

// ====================================================================
// 3. CATEGORIES MANAGEMENT SECTION
// ====================================================================

@Composable
fun CategoriesManagementSection(
    categories: List<MuscleCategory>,
    onAddCustomCategory: (name: String, colorHex: Long) -> Unit,
    onDeleteCustomCategory: (id: String) -> Unit
) {
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<MuscleCategory?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categorías musculares",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Button(
                onClick = { showAddCategoryDialog = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Crear personalizada", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories, key = { it.id }) { cat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(cat.color)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cat.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (cat.isCustom) "Personalizada por el usuario" else "Categoría estándar",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (cat.isCustom) {
                            IconButton(onClick = { categoryToDelete = cat }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddCategoryDialog) {
        CustomCategoryDialog(
            onConfirm = { catName, hex ->
                onAddCustomCategory(catName, hex)
                showAddCategoryDialog = false
            },
            onDismiss = { showAddCategoryDialog = false }
        )
    }

    categoryToDelete?.let { cat ->
        ConfirmDialog(
            title = "Eliminar Categoría",
            message = "¿Deseas eliminar la categoría personalizada \"${cat.name}\"?",
            confirmText = "Eliminar",
            isDestructive = true,
            onConfirm = {
                onDeleteCustomCategory(cat.id)
                categoryToDelete = null
            },
            onDismiss = { categoryToDelete = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomCategoryDialog(
    onConfirm: (name: String, colorHex: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    val colorPalette = listOf(
        0xFFE53935L, // Red
        0xFFD81B60L, // Pink
        0xFF8E24AAL, // Purple
        0xFF5E35B1L, // Deep Purple
        0xFF3949ABL, // Indigo
        0xFF1E88E5L, // Blue
        0xFF039BE5L, // Light Blue
        0xFF00ACC1L, // Cyan
        0xFF00897BL, // Teal
        0xFF43A047L, // Green
        0xFF7CB342L, // Lime
        0xFFFDD835L, // Yellow
        0xFFFB8C00L, // Orange
        0xFFF4511EL  // Deep Orange
    )
    var selectedColor by remember { mutableStateOf(colorPalette[5]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Categoría Personalizada", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la categoría") },
                    placeholder = { Text("Ej. Trapecio, Movilidad...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Selecciona un color identificativo:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorPalette.forEach { hex ->
                        val isSelected = hex == selectedColor
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .clickable { selectedColor = hex }
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isNotEmpty()) {
                        onConfirm(name.trim(), selectedColor)
                    }
                }
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// ====================================================================
// 4. WEEKLY MARKS (RESET MARKS)
// ====================================================================

@Composable
fun WeeklyMarksSection(
    onResetWeeklyMarks: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Marcas de la semana",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Control del progreso semanal",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Al reiniciar las marcas de la semana, se restablecerán a 'pendiente' todas las series que hayas marcado como completadas durante tus entrenamientos.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Guarantee card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "✓ Lo que NO se eliminará:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Tus días de entrenamiento configurados\n• Tus ejercicios y categorías\n• Tus pesos y cantidades de series\n• Tus temporizadores guardados",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reiniciar marcas de la semana",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    if (showConfirmDialog) {
        ConfirmDialog(
            title = "Reiniciar marcas semanales",
            message = "¿Confirmas que deseas reiniciar las marcas de la semana? Todas las series volverán a quedar pendientes para la nueva semana.",
            confirmText = "Reiniciar marcas",
            isDestructive = true,
            onConfirm = {
                onResetWeeklyMarks()
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

@Composable
fun EmptySectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
