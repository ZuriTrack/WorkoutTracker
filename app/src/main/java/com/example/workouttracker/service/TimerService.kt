package com.example.workouttracker.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.example.workouttracker.model.ActiveSession
import com.example.workouttracker.model.SessionType
import com.example.workouttracker.model.WorkoutPhase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Temporizador"
                val duration = intent.getIntExtra(EXTRA_DURATION, 60)
                startCountdown(title, duration)
            }
            ACTION_STOP -> {
                stopCountdown()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startCountdown(title: String, duration: Int) {
        timerJob?.cancel()

        val notification = TimerNotificationHelper.buildTimerNotification(
            this,
            title,
            "Tiempo restante: ${formatTime(duration)}",
            duration,
            duration
        )
        try {
            startForeground(TimerNotificationHelper.NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        timerJob = serviceScope.launch {
            var remaining = duration
            while (remaining > 0) {
                delay(1000L)
                remaining--
                val updatedNotification = TimerNotificationHelper.buildTimerNotification(
                    this@TimerService,
                    title,
                    "Tiempo restante: ${formatTime(remaining)}",
                    remaining,
                    duration
                )
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.notify(TimerNotificationHelper.NOTIFICATION_ID, updatedNotification)
            }

            // Finished!
            TimerNotificationHelper.triggerVibration(this@TimerService)
            TimerNotificationHelper.showFinishedNotification(
                this@TimerService,
                "¡Tiempo completado!",
                "$title ha finalizado."
            )
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopCountdown() {
        timerJob?.cancel()
        timerJob = null
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
    }

    private fun formatTime(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return String.format("%02d:%02d", m, s)
    }

    companion object {
        const val ACTION_START = "com.example.workouttracker.START_TIMER"
        const val ACTION_STOP = "com.example.workouttracker.STOP_TIMER"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DURATION = "extra_duration"

        fun startService(context: Context, title: String, durationSeconds: Int) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_DURATION, durationSeconds)
            }
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
