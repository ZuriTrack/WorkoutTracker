package com.example.workouttracker.model

import androidx.compose.ui.graphics.Color
import org.json.JSONObject

data class MuscleCategory(
    val id: String,
    val name: String,
    val colorHex: Long,
    val isCustom: Boolean = false
) {
    val color: Color
        get() = Color(colorHex)

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("colorHex", colorHex)
            put("isCustom", isCustom)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): MuscleCategory {
            return MuscleCategory(
                id = json.getString("id"),
                name = json.getString("name"),
                colorHex = json.getLong("colorHex"),
                isCustom = json.optBoolean("isCustom", false)
            )
        }

        // Standard 11 muscle categories with distinct identifying colors
        val DEFAULT_CATEGORIES = listOf(
            MuscleCategory(id = "cat_pecho", name = "Pecho", colorHex = 0xFFE53935L),       // Vivid Red
            MuscleCategory(id = "cat_espalda", name = "Espalda", colorHex = 0xFF1E88E5L),   // Vivid Blue
            MuscleCategory(id = "cat_hombros", name = "Hombros", colorHex = 0xFFFB8C00L),   // Amber Orange
            MuscleCategory(id = "cat_triceps", name = "Tríceps", colorHex = 0xFF8E24AAL),   // Purple
            MuscleCategory(id = "cat_biceps", name = "Bíceps", colorHex = 0xFF00ACC1L),     // Cyan Teal
            MuscleCategory(id = "cat_antebrazos", name = "Antebrazos", colorHex = 0xFF3949ABL), // Indigo
            MuscleCategory(id = "cat_piernas", name = "Piernas", colorHex = 0xFF43A047L),   // Green
            MuscleCategory(id = "cat_gluteos", name = "Glúteos", colorHex = 0xFFD81B60L),   // Pink Magenta
            MuscleCategory(id = "cat_gemelos", name = "Gemelos", colorHex = 0xFF7CB342L),   // Lime Green
            MuscleCategory(id = "cat_core", name = "Core", colorHex = 0xFFF4511EL),         // Deep Orange
            MuscleCategory(id = "cat_cardio", name = "Cardio", colorHex = 0xFFFDD835L)      // Gold Yellow
        )
    }
}
