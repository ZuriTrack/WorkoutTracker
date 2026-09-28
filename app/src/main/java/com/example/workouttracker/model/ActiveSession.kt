package com.example.workouttracker.model

enum class SessionType {
    WORKOUT_SET,
    CLOCK_TIMER
}

enum class WorkoutPhase {
    WORK,
    REST
}

data class ActiveSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: SessionType = SessionType.WORKOUT_SET,
    val exerciseId: String? = null,
    val exerciseName: String = "",
    val categoryColorHex: Long = 0xFF43A047L,
    val setIndex: Int = 0,
    val totalSets: Int = 1,
    val phase: WorkoutPhase = WorkoutPhase.WORK,
    val totalSeconds: Int = 60,
    val remainingSeconds: Int = 60,
    val isRunning: Boolean = false,
    val workTimeSeconds: Int = 0,
    val restTimeSeconds: Int = 0,
    val standaloneTimerId: String? = null,
    val standaloneTimerName: String? = null,
    val targetEndTimeMillis: Long = 0L
) {
    val progress: Float
        get() = if (totalSeconds > 0) {
            1f - (remainingSeconds.toFloat() / totalSeconds.toFloat())
        } else 0f

    val formattedRemaining: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}
