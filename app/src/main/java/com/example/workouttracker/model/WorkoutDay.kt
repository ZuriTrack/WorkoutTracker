package com.example.workouttracker.model

import org.json.JSONObject

data class WorkoutDay(
    val id: String,
    val name: String,
    val dayOfWeek: String,
    val order: Int = 0
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("dayOfWeek", dayOfWeek)
            put("order", order)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): WorkoutDay {
            return WorkoutDay(
                id = json.getString("id"),
                name = json.getString("name"),
                dayOfWeek = json.optString("dayOfWeek", "Lunes"),
                order = json.optInt("order", 0)
            )
        }

        val DAYS_OF_WEEK = listOf(
            "Lunes",
            "Martes",
            "Miércoles",
            "Jueves",
            "Viernes",
            "Sábado",
            "Domingo",
            "Personalizado"
        )
    }
}
