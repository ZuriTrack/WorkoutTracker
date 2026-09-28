package com.example.workouttracker.model

import org.json.JSONObject

data class WorkoutTimer(
    val id: String,
    val name: String,
    val durationSeconds: Int,
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedDuration: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("durationSeconds", durationSeconds)
            put("createdAt", createdAt)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): WorkoutTimer {
            return WorkoutTimer(
                id = json.getString("id"),
                name = json.getString("name"),
                durationSeconds = json.getInt("durationSeconds"),
                createdAt = json.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}
