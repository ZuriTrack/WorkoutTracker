package com.example.workouttracker.ui.routine

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.model.ActiveSession
import com.example.workouttracker.model.Exercise
import com.example.workouttracker.model.MuscleCategory
import com.example.workouttracker.model.WorkoutDay
import com.example.workouttracker.ui.components.CategoryBadge

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoutineScreen(
    days: List<WorkoutDay>,
    exercises: List<Exercise>,
    categories: List<MuscleCategory>,
    weeklyMarks: Map<String, Boolean>,
    activeSession: ActiveSession?,
    selectedDayId: String?,
    onSelectDay: (String) -> Unit,
    onSetFinished: (exercise: Exercise, categoryColorHex: Long, setIndex: Int) -> Unit,
    onToggleSetCompleted: (exerciseId: String, setIndex: Int) -> Unit,
    onNavigateToSettings: (targetDayId: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Current selected day
    val effectiveDayId = if (selectedDayId != null && days.any { it.id == selectedDayId }) {
        selectedDayId
    } else {
        days.firstOrNull()?.id
    }

    val selectedDay = days.find { it.id == effectiveDayId }
    val currentDayExercises = if (effectiveDayId != null) {
        exercises.filter { it.dayId == effectiveDayId }.sortedBy { it.order }
    } else {
        emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App header
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
                text = "Mi Rutina",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Toca cada serie al terminar para registrarla y descansar",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Days selector bar
        if (days.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEach { day ->
                    val isSelected = day.id == effectiveDayId
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectDay(day.id) },
                        label = {
                            Text(
                                text = day.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Exercises list or Empty State
        if (days.isEmpty() || currentDayExercises.isEmpty()) {
            EmptyRoutineView(
                hasDays = days.isNotEmpty(),
                dayName = selectedDay?.name,
                onAddExercisesClick = {
                    onNavigateToSettings(effectiveDayId)
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(currentDayExercises, key = { it.id }) { exercise ->
                    val category = categories.find { it.id == exercise.categoryId }
                        ?: MuscleCategory.DEFAULT_CATEGORIES.first()

                    ExerciseCard(
                        exercise = exercise,
                        category = category,
                        weeklyMarks = weeklyMarks,
                        activeSession = activeSession,
                        onSetClick = { setIndex ->
                            val key = "${exercise.id}-$setIndex"
                            val isCompleted = weeklyMarks[key] == true
                            if (isCompleted) {
                                // Already completed -> toggle back to pending
                                onToggleSetCompleted(exercise.id, setIndex)
                            } else {
                                // Not completed -> Finish set, mark it, and start rest timer
                                onSetFinished(exercise, category.colorHex, setIndex)
                            }
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyRoutineView(
    hasDays: Boolean,
    dayName: String?,
    onAddExercisesClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "No hay ejercicios configurados",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (hasDays && dayName != null) {
                        "El día \"$dayName\" no tiene ejercicios asignados todavía."
                    } else {
                        "Comienza agregando días de entrenamiento y ejercicios en la sección de Ajustes."
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onAddExercisesClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Agregar ejercicios",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseCard(
    exercise: Exercise,
    category: MuscleCategory,
    weeklyMarks: Map<String, Boolean>,
    activeSession: ActiveSession?,
    onSetClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Row: Name and Category Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                CategoryBadge(category = category)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-row: Weight & Rest time information
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Weight tag
                if (exercise.weight != null && exercise.weight > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Peso: ",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${exercise.weight} ${exercise.weightUnit}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Sets count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Series: ",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${exercise.setsCount}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Rest Time info
                if (exercise.restTimeSeconds > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Descanso: ${exercise.restTimeSeconds}s",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sets row (Serie 1 ○, Serie 2 ○, Serie 3 ○)
            Text(
                text = "Toca la serie al terminarla para completarla e iniciar descanso:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (setIndex in 0 until exercise.setsCount) {
                    val key = "${exercise.id}-$setIndex"
                    val isCompleted = weeklyMarks[key] == true
                    val isTimingThis = activeSession?.exerciseId == exercise.id &&
                            activeSession.setIndex == setIndex &&
                            activeSession.isRunning

                    SetChip(
                        setNumber = setIndex + 1,
                        isCompleted = isCompleted,
                        isTiming = isTimingThis,
                        remainingSeconds = if (isTimingThis) activeSession?.remainingSeconds else null,
                        categoryColor = category.color,
                        onClick = { onSetClick(setIndex) }
                    )
                }
            }
        }
    }
}

@Composable
fun SetChip(
    setNumber: Int,
    isCompleted: Boolean,
    isTiming: Boolean,
    remainingSeconds: Int?,
    categoryColor: Color,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isTiming -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
        isCompleted -> Color(0xFF2E7D32).copy(alpha = 0.15f) // Success green tint
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }

    val borderColor = when {
        isTiming -> MaterialTheme.colorScheme.tertiary
        isCompleted -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    val contentColor = when {
        isTiming -> MaterialTheme.colorScheme.tertiary
        isCompleted -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isTiming || isCompleted) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Serie $setNumber",
                fontSize = 13.sp,
                fontWeight = if (isTiming || isCompleted) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )

            // Status icon: ○ (pending), ✓ (completed), ⏱ (resting countdown)
            when {
                isTiming && remainingSeconds != null -> {
                    Text(
                        text = "${remainingSeconds}s",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                isCompleted -> {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completada",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                else -> {
                    // Outlined circle ○
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }
        }
    }
}
