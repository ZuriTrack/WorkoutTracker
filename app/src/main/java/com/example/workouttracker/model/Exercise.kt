package com.example.workouttracker.model

import org.json.JSONObject

data class Exercise(
    val id: String,
    val dayId: String,
    val name: String,
    val categoryId: String,
    val weight: Double? = null,
    val weightUnit: String = "kg",
    val setsCount: Int = 3,
    val workTimeSeconds: Int = 0,    // 0 means no work timer configured
    val restTimeSeconds: Int = 0,    // 0 means no rest timer configured
    val timerId: String? = null,     // Optional linked timer
    val order: Int = 0
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("dayId", dayId)
            put("name", name)
            put("categoryId", categoryId)
            if (weight != null) put("weight", weight)
            put("weightUnit", weightUnit)
            put("setsCount", setsCount)
            put("workTimeSeconds", workTimeSeconds)
            put("restTimeSeconds", restTimeSeconds)
            if (timerId != null) put("timerId", timerId)
            put("order", order)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): Exercise {
            return Exercise(
                id = json.getString("id"),
                dayId = json.getString("dayId"),
                name = json.getString("name"),
                categoryId = json.getString("categoryId"),
                weight = if (json.has("weight") && !json.isNull("weight")) json.getDouble("weight") else null,
                weightUnit = json.optString("weightUnit", "kg"),
                setsCount = json.optInt("setsCount", 3),
                workTimeSeconds = json.optInt("workTimeSeconds", 0),
                restTimeSeconds = json.optInt("restTimeSeconds", 0),
                timerId = if (json.has("timerId") && !json.isNull("timerId")) json.getString("timerId") else null,
                order = json.optInt("order", 0)
            )
        }
    }
}
