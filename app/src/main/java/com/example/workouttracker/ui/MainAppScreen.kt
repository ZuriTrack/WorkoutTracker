package com.example.workouttracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.WorkoutRepository
import com.example.workouttracker.service.TimerController
import com.example.workouttracker.ui.clock.ClockScreen
import com.example.workouttracker.ui.components.ActiveTimerModal
import com.example.workouttracker.ui.components.MiniTimerBar
import com.example.workouttracker.ui.routine.RoutineScreen
import com.example.workouttracker.ui.settings.SettingsScreen

enum class MainTab(val title: String) {
    RUTINA("Rutina"),
    RELOJ("Reloj"),
    AJUSTES("Ajustes")
}

@Composable
fun MainAppScreen() {
    val context = LocalContext.current
    val repository = remember { WorkoutRepository.getInstance(context) }
    val timerController = remember { TimerController.getInstance(context) }

    val days by repository.days.collectAsState()
    val exercises by repository.exercises.collectAsState()
    val categories by repository.allCategories.collectAsState()
    val timers by repository.timers.collectAsState()
    val weeklyMarks by repository.weeklyMarks.collectAsState()
    val activeSession by timerController.activeSession.collectAsState()

    var selectedTab by remember { mutableStateOf(MainTab.RUTINA) }
    var selectedRoutineDayId by remember { mutableStateOf<String?>(null) }
    var settingsInitialDayId by remember { mutableStateOf<String?>(null) }

    var showActiveModal by remember { mutableStateOf(false) }

    // When a new timer session begins, show full modal automatically
    androidx.compose.runtime.LaunchedEffect(activeSession?.id) {
        if (activeSession != null) {
            showActiveModal = true
        }
    }

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Mini floating timer bar above nav bar if timer active and modal minimized
                AnimatedVisibility(
                    visible = activeSession != null && !showActiveModal,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    activeSession?.let { session ->
                        MiniTimerBar(
                            session = session,
                            onClick = { showActiveModal = true },
                            onTogglePlayPause = {
                                if (session.isRunning) timerController.pause() else timerController.resume()
                            },
                            onClose = { timerController.stop() }
                        )
                    }
                }

                // Strictly the 3 required tabs: Rutina, Reloj, Ajustes
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == MainTab.RUTINA,
                        onClick = { selectedTab = MainTab.RUTINA },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == MainTab.RUTINA) Icons.Filled.FitnessCenter else Icons.Outlined.FitnessCenter,
                                contentDescription = "Rutina"
                            )
                        },
                        label = {
                            Text(
                                text = "Rutina",
                                fontWeight = if (selectedTab == MainTab.RUTINA) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == MainTab.RELOJ,
                        onClick = { selectedTab = MainTab.RELOJ },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == MainTab.RELOJ) Icons.Filled.Timer else Icons.Outlined.Timer,
                                contentDescription = "Reloj"
                            )
                        },
                        label = {
                            Text(
                                text = "Reloj",
                                fontWeight = if (selectedTab == MainTab.RELOJ) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == MainTab.AJUSTES,
                        onClick = { selectedTab = MainTab.AJUSTES },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == MainTab.AJUSTES) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Ajustes"
                            )
                        },
                        label = {
                            Text(
                                text = "Ajustes",
                                fontWeight = if (selectedTab == MainTab.AJUSTES) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.RUTINA -> RoutineScreen(
                    days = days,
                    exercises = exercises,
                    categories = categories,
                    weeklyMarks = weeklyMarks,
                    activeSession = activeSession,
                    selectedDayId = selectedRoutineDayId,
                    onSelectDay = { selectedRoutineDayId = it },
                    onSetFinished = { exercise, colorHex, setIndex ->
                        timerController.onSetFinished(exercise, colorHex, setIndex)
                        if (exercise.restTimeSeconds > 0) {
                            showActiveModal = true
                        }
                    },
                    onToggleSetCompleted = { exerciseId, setIndex ->
                        repository.toggleSetCompleted(exerciseId, setIndex)
                    },
                    onNavigateToSettings = { targetDayId ->
                        settingsInitialDayId = targetDayId
                        selectedTab = MainTab.AJUSTES
                    }
                )

                MainTab.RELOJ -> ClockScreen(
                    timers = timers,
                    activeSession = activeSession,
                    onStartTimer = { timer ->
                        timerController.startStandaloneTimer(timer)
                        showActiveModal = true
                    },
                    onAddTimer = { name, durationSec ->
                        repository.addTimer(name, durationSec)
                    },
                    onUpdateTimer = { id, name, durationSec ->
                        repository.updateTimer(id, name, durationSec)
                    },
                    onDeleteTimer = { id ->
                        repository.deleteTimer(id)
                    }
                )

                MainTab.AJUSTES -> SettingsScreen(
                    days = days,
                    exercises = exercises,
                    categories = categories,
                    timers = timers,
                    initialDayId = settingsInitialDayId,
                    onAddDay = { name, dow ->
                        val created = repository.addDay(name, dow)
                        selectedRoutineDayId = created.id
                    },
                    onUpdateDay = { id, name, dow ->
                        repository.updateDay(id, name, dow)
                    },
                    onDeleteDay = { id ->
                        repository.deleteDay(id)
                        if (selectedRoutineDayId == id) selectedRoutineDayId = null
                    },
                    onReorderDays = { newDays ->
                        repository.reorderDays(newDays)
                    },
                    onAddExercise = { dayId, name, catId, weight, unit, sets, workTime, restTime, timerId ->
                        repository.addExercise(dayId, name, catId, weight, unit, sets, workTime, restTime, timerId)
                    },
                    onUpdateExercise = { ex ->
                        repository.updateExercise(ex)
                    },
                    onDeleteExercise = { id ->
                        repository.deleteExercise(id)
                    },
                    onReorderExercises = { dayId, list ->
                        repository.reorderExercises(dayId, list)
                    },
                    onAddCustomCategory = { name, colorHex ->
                        repository.addCustomCategory(name, colorHex)
                    },
                    onDeleteCustomCategory = { id ->
                        repository.deleteCustomCategory(id)
                    },
                    onResetWeeklyMarks = {
                        repository.resetWeeklyMarks()
                    }
                )
            }
        }
    }

    // Active Timer Modal / Dialog
    if (showActiveModal && activeSession != null) {
        activeSession?.let { session ->
            ActiveTimerModal(
                session = session,
                onPause = { timerController.pause() },
                onResume = { timerController.resume() },
                onSkipOrFinishEarly = {
                    timerController.skipRestTimer()
                    showActiveModal = false
                },
                onStop = {
                    timerController.stop()
                    showActiveModal = false
                },
                onDismissRequest = { showActiveModal = false }
            )
        }
    }
}
