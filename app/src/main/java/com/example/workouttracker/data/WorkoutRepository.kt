package com.example.workouttracker.data

import android.content.Context
import android.content.SharedPreferences
import com.example.workouttracker.model.Exercise
import com.example.workouttracker.model.MuscleCategory
import com.example.workouttracker.model.WorkoutDay
import com.example.workouttracker.model.WorkoutTimer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class WorkoutRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("workout_tracker_prefs", Context.MODE_PRIVATE)

    private val _days = MutableStateFlow<List<WorkoutDay>>(emptyList())
    val days: StateFlow<List<WorkoutDay>> = _days.asStateFlow()

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

    private val _customCategories = MutableStateFlow<List<MuscleCategory>>(emptyList())
    val customCategories: StateFlow<List<MuscleCategory>> = _customCategories.asStateFlow()

    private val _allCategories = MutableStateFlow<List<MuscleCategory>>(MuscleCategory.DEFAULT_CATEGORIES)
    val allCategories: StateFlow<List<MuscleCategory>> = _allCategories.asStateFlow()

    private val _timers = MutableStateFlow<List<WorkoutTimer>>(emptyList())
    val timers: StateFlow<List<WorkoutTimer>> = _timers.asStateFlow()

    // Key format: "$exerciseId-$setIndex" -> true/false
    private val _weeklyMarks = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val weeklyMarks: StateFlow<Map<String, Boolean>> = _weeklyMarks.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // 1. Days
        val daysJson = prefs.getString(KEY_DAYS, null)
        val loadedDays = mutableListOf<WorkoutDay>()
        if (daysJson != null) {
            try {
                val array = JSONArray(daysJson)
                for (i in 0 until array.length()) {
                    loadedDays.add(WorkoutDay.fromJson(array.getJSONObject(i)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _days.value = loadedDays.sortedBy { it.order }

        // 2. Custom Categories
        val categoriesJson = prefs.getString(KEY_CUSTOM_CATEGORIES, null)
        val loadedCategories = mutableListOf<MuscleCategory>()
        if (categoriesJson != null) {
            try {
                val array = JSONArray(categoriesJson)
                for (i in 0 until array.length()) {
                    loadedCategories.add(MuscleCategory.fromJson(array.getJSONObject(i)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _customCategories.value = loadedCategories
        updateAllCategoriesList()

        // 3. Exercises
        val exercisesJson = prefs.getString(KEY_EXERCISES, null)
        val loadedExercises = mutableListOf<Exercise>()
        if (exercisesJson != null) {
            try {
                val array = JSONArray(exercisesJson)
                for (i in 0 until array.length()) {
                    loadedExercises.add(Exercise.fromJson(array.getJSONObject(i)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _exercises.value = loadedExercises.sortedBy { it.order }

        // 4. Timers
        val timersJson = prefs.getString(KEY_TIMERS, null)
        val loadedTimers = mutableListOf<WorkoutTimer>()
        if (timersJson != null) {
            try {
                val array = JSONArray(timersJson)
                for (i in 0 until array.length()) {
                    loadedTimers.add(WorkoutTimer.fromJson(array.getJSONObject(i)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _timers.value = loadedTimers

        // 5. Weekly Marks
        val marksJson = prefs.getString(KEY_WEEKLY_MARKS, null)
        val loadedMarks = mutableMapOf<String, Boolean>()
        if (marksJson != null) {
            try {
                val obj = JSONObject(marksJson)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    loadedMarks[key] = obj.getBoolean(key)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _weeklyMarks.value = loadedMarks
    }

    private fun updateAllCategoriesList() {
        _allCategories.value = MuscleCategory.DEFAULT_CATEGORIES + _customCategories.value
    }

    // ==========================================
    // DAYS MANAGEMENT
    // ==========================================

    fun addDay(name: String, dayOfWeek: String): WorkoutDay {
        val newOrder = (_days.value.maxOfOrNull { it.order } ?: -1) + 1
        val newDay = WorkoutDay(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            dayOfWeek = dayOfWeek,
            order = newOrder
        )
        val updated = _days.value + newDay
        _days.value = updated
        saveDays(updated)
        return newDay
    }

    fun updateDay(id: String, name: String, dayOfWeek: String) {
        val updated = _days.value.map { day ->
            if (day.id == id) {
                day.copy(name = name.trim(), dayOfWeek = dayOfWeek)
            } else {
                day
            }
        }
        _days.value = updated
        saveDays(updated)
    }

    fun deleteDay(id: String) {
        val updatedDays = _days.value.filter { it.id != id }
        _days.value = updatedDays
        saveDays(updatedDays)

        // Delete associated exercises as well
        val remainingExercises = _exercises.value.filter { it.dayId != id }
        _exercises.value = remainingExercises
        saveExercises(remainingExercises)
    }

    fun reorderDays(newOrder: List<WorkoutDay>) {
        val updated = newOrder.mapIndexed { index, day -> day.copy(order = index) }
        _days.value = updated
        saveDays(updated)
    }

    private fun saveDays(list: List<WorkoutDay>) {
        val array = JSONArray()
        list.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_DAYS, array.toString()).apply()
    }

    // ==========================================
    // EXERCISE MANAGEMENT
    // ==========================================

    fun addExercise(
        dayId: String,
        name: String,
        categoryId: String,
        weight: Double?,
        weightUnit: String = "kg",
        setsCount: Int,
        workTimeSeconds: Int = 0,
        restTimeSeconds: Int = 0,
        timerId: String? = null
    ): Exercise {
        val currentDayExercises = _exercises.value.filter { it.dayId == dayId }
        val newOrder = (currentDayExercises.maxOfOrNull { it.order } ?: -1) + 1
        val newExercise = Exercise(
            id = UUID.randomUUID().toString(),
            dayId = dayId,
            name = name.trim(),
            categoryId = categoryId,
            weight = weight,
            weightUnit = weightUnit,
            setsCount = setsCount.coerceAtLeast(1),
            workTimeSeconds = workTimeSeconds,
            restTimeSeconds = restTimeSeconds,
            timerId = timerId,
            order = newOrder
        )
        val updated = _exercises.value + newExercise
        _exercises.value = updated
        saveExercises(updated)
        return newExercise
    }

    fun updateExercise(exercise: Exercise) {
        val updated = _exercises.value.map {
            if (it.id == exercise.id) exercise else it
        }
        _exercises.value = updated
        saveExercises(updated)
    }

    fun deleteExercise(id: String) {
        val updated = _exercises.value.filter { it.id != id }
        _exercises.value = updated
        saveExercises(updated)

        // Clean marks for this exercise
        val currentMarks = _weeklyMarks.value.toMutableMap()
        val toRemove = currentMarks.keys.filter { it.startsWith("$id-") }
        toRemove.forEach { currentMarks.remove(it) }
        _weeklyMarks.value = currentMarks
        saveWeeklyMarks(currentMarks)
    }

    fun reorderExercises(dayId: String, reorderedList: List<Exercise>) {
        val otherDayExercises = _exercises.value.filter { it.dayId != dayId }
        val updatedCurrentDay = reorderedList.mapIndexed { index, exercise ->
            exercise.copy(order = index)
        }
        val combined = otherDayExercises + updatedCurrentDay
        _exercises.value = combined
        saveExercises(combined)
    }

    private fun saveExercises(list: List<Exercise>) {
        val array = JSONArray()
        list.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_EXERCISES, array.toString()).apply()
    }

    // ==========================================
    // CUSTOM CATEGORIES
    // ==========================================

    fun addCustomCategory(name: String, colorHex: Long): MuscleCategory {
        val newCat = MuscleCategory(
            id = "cat_custom_${UUID.randomUUID()}",
            name = name.trim(),
            colorHex = colorHex,
            isCustom = true
        )
        val updated = _customCategories.value + newCat
        _customCategories.value = updated
        updateAllCategoriesList()
        saveCustomCategories(updated)
        return newCat
    }

    fun deleteCustomCategory(id: String) {
        val updated = _customCategories.value.filter { it.id != id }
        _customCategories.value = updated
        updateAllCategoriesList()
        saveCustomCategories(updated)
    }

    private fun saveCustomCategories(list: List<MuscleCategory>) {
        val array = JSONArray()
        list.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_CUSTOM_CATEGORIES, array.toString()).apply()
    }

    // ==========================================
    // TIMERS MANAGEMENT
    // ==========================================

    fun addTimer(name: String, durationSeconds: Int): WorkoutTimer {
        val newTimer = WorkoutTimer(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            durationSeconds = durationSeconds.coerceAtLeast(1)
        )
        val updated = _timers.value + newTimer
        _timers.value = updated
        saveTimers(updated)
        return newTimer
    }

    fun updateTimer(id: String, name: String, durationSeconds: Int) {
        val updated = _timers.value.map {
            if (it.id == id) {
                it.copy(name = name.trim(), durationSeconds = durationSeconds.coerceAtLeast(1))
            } else it
        }
        _timers.value = updated
        saveTimers(updated)
    }

    fun deleteTimer(id: String) {
        val updated = _timers.value.filter { it.id != id }
        _timers.value = updated
        saveTimers(updated)
    }

    private fun saveTimers(list: List<WorkoutTimer>) {
        val array = JSONArray()
        list.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_TIMERS, array.toString()).apply()
    }

    // ==========================================
    // WEEKLY MARKS (PROGRESS)
    // ==========================================

    fun isSetCompleted(exerciseId: String, setIndex: Int): Boolean {
        val key = "$exerciseId-$setIndex"
        return _weeklyMarks.value[key] == true
    }

    fun toggleSetCompleted(exerciseId: String, setIndex: Int): Boolean {
        val key = "$exerciseId-$setIndex"
        val currentStatus = _weeklyMarks.value[key] ?: false
        val newStatus = !currentStatus
        markSetCompleted(exerciseId, setIndex, newStatus)
        return newStatus
    }

    fun markSetCompleted(exerciseId: String, setIndex: Int, completed: Boolean) {
        val key = "$exerciseId-$setIndex"
        val updated = _weeklyMarks.value.toMutableMap()
        if (completed) {
            updated[key] = true
        } else {
            updated.remove(key)
        }
        _weeklyMarks.value = updated
        saveWeeklyMarks(updated)
    }

    /**
     * Resets ONLY the weekly marks.
     * Keeps all days, exercises, sets, weights, timers and categories intact.
     */
    fun resetWeeklyMarks() {
        _weeklyMarks.value = emptyMap()
        prefs.edit().remove(KEY_WEEKLY_MARKS).apply()
    }

    private fun saveWeeklyMarks(map: Map<String, Boolean>) {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit().putString(KEY_WEEKLY_MARKS, obj.toString()).apply()
    }

    fun getCategoryById(id: String): MuscleCategory {
        return _allCategories.value.find { it.id == id }
            ?: MuscleCategory.DEFAULT_CATEGORIES.first()
    }

    companion object {
        private const val KEY_DAYS = "saved_days"
        private const val KEY_EXERCISES = "saved_exercises"
        private const val KEY_CUSTOM_CATEGORIES = "saved_custom_categories"
        private const val KEY_TIMERS = "saved_timers"
        private const val KEY_WEEKLY_MARKS = "saved_weekly_marks"

        @Volatile
        private var INSTANCE: WorkoutRepository? = null

        fun getInstance(context: Context): WorkoutRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WorkoutRepository(context).also { INSTANCE = it }
            }
        }
    }
}
