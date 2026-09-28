package com.example.workouttracker.service

import android.content.Context
import com.example.workouttracker.data.WorkoutRepository
import com.example.workouttracker.model.ActiveSession
import com.example.workouttracker.model.Exercise
import com.example.workouttracker.model.SessionType
import com.example.workouttracker.model.WorkoutPhase
import com.example.workouttracker.model.WorkoutTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerController private constructor(private val context: Context) {

    private val repository = WorkoutRepository.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var tickerJob: Job? = null

    private val _activeSession = MutableStateFlow<ActiveSession?>(null)
    val activeSession: StateFlow<ActiveSession?> = _activeSession.asStateFlow()

    /**
     * Tapping a set marks it completed immediately and starts the REST timer
     */
    fun onSetFinished(
        exercise: Exercise,
        categoryColorHex: Long,
        setIndex: Int
    ) {
        tickerJob?.cancel()

        // 1. Immediately mark the set as completed in the repository
        repository.markSetCompleted(exercise.id, setIndex, true)

        // 2. If the exercise has a rest time configured, start the rest timer
        val restSec = exercise.restTimeSeconds
        if (restSec > 0) {
            val session = ActiveSession(
                type = SessionType.WORKOUT_SET,
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                categoryColorHex = categoryColorHex,
                setIndex = setIndex,
                totalSets = exercise.setsCount,
                phase = WorkoutPhase.REST,
                totalSeconds = restSec,
                remainingSeconds = restSec,
                isRunning = true,
                workTimeSeconds = 0,
                restTimeSeconds = restSec,
                targetEndTimeMillis = System.currentTimeMillis() + (restSec * 1000L)
            )
            _activeSession.value = session

            TimerService.startService(
                context,
                "${exercise.name} - Descanso Serie ${setIndex + 1}",
                restSec
            )

            startTicker()
        } else {
            // No rest timer configured, just confirm completion
            stop()
            TimerNotificationHelper.triggerVibration(context)
        }
    }

    fun startStandaloneTimer(timer: WorkoutTimer) {
        tickerJob?.cancel()

        val session = ActiveSession(
            type = SessionType.CLOCK_TIMER,
            standaloneTimerId = timer.id,
            standaloneTimerName = timer.name,
            exerciseName = timer.name,
            totalSeconds = timer.durationSeconds,
            remainingSeconds = timer.durationSeconds,
            isRunning = true,
            targetEndTimeMillis = System.currentTimeMillis() + (timer.durationSeconds * 1000L)
        )
        _activeSession.value = session

        TimerService.startService(
            context,
            timer.name,
            timer.durationSeconds
        )

        startTicker()
    }

    fun startCustomClockTimer(name: String, durationSeconds: Int) {
        tickerJob?.cancel()

        val safeSec = durationSeconds.coerceAtLeast(1)
        val session = ActiveSession(
            type = SessionType.CLOCK_TIMER,
            standaloneTimerName = name,
            exerciseName = name,
            totalSeconds = safeSec,
            remainingSeconds = safeSec,
            isRunning = true,
            targetEndTimeMillis = System.currentTimeMillis() + (safeSec * 1000L)
        )
        _activeSession.value = session

        TimerService.startService(
            context,
            name,
            safeSec
        )

        startTicker()
    }

    fun pause() {
        val current = _activeSession.value ?: return
        if (!current.isRunning) return
        tickerJob?.cancel()
        tickerJob = null
        _activeSession.value = current.copy(isRunning = false)
        TimerService.stopService(context)
    }

    fun resume() {
        val current = _activeSession.value ?: return
        if (current.isRunning) return
        val newTarget = System.currentTimeMillis() + (current.remainingSeconds * 1000L)
        _activeSession.value = current.copy(
            isRunning = true,
            targetEndTimeMillis = newTarget
        )

        val title = when (current.type) {
            SessionType.WORKOUT_SET -> "${current.exerciseName} - Descanso Serie ${current.setIndex + 1}"
            SessionType.CLOCK_TIMER -> current.standaloneTimerName ?: "Temporizador"
        }
        TimerService.startService(context, title, current.remainingSeconds)
        startTicker()
    }

    fun skipRestTimer() {
        stop()
    }

    fun stop() {
        tickerJob?.cancel()
        tickerJob = null
        _activeSession.value = null
        TimerService.stopService(context)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (true) {
                delay(500L)
                val current = _activeSession.value ?: break
                if (!current.isRunning) break

                val now = System.currentTimeMillis()
                val remaining = ((current.targetEndTimeMillis - now + 999L) / 1000L).toInt().coerceAtLeast(0)

                if (remaining <= 0) {
                    _activeSession.value = current.copy(remainingSeconds = 0)
                    handleTimerCompletion(current)
                    break
                } else {
                    _activeSession.value = current.copy(remainingSeconds = remaining)
                }
            }
        }
    }

    private fun handleTimerCompletion(session: ActiveSession) {
        TimerNotificationHelper.triggerVibration(context)

        if (session.type == SessionType.WORKOUT_SET) {
            val nextSet = session.setIndex + 2
            val nextMsg = if (nextSet <= session.totalSets) {
                "${session.exerciseName}: Descanso finalizado. ¡Listo para Serie $nextSet!"
            } else {
                "${session.exerciseName}: ¡Todas las series completadas!"
            }

            TimerNotificationHelper.showFinishedNotification(
                context,
                "¡Descanso finalizado!",
                nextMsg
            )
            stop()
        } else {
            // Standalone clock timer
            TimerNotificationHelper.showFinishedNotification(
                context,
                "¡Temporizador finalizado!",
                "${session.standaloneTimerName ?: "Temporizador"} ha completado el tiempo."
            )
            stop()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: TimerController? = null

        fun getInstance(context: Context): TimerController {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TimerController(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
