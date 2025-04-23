/*
This ViewModel was developed using assistance from ChatGPT (April 2025),
    which provided implementation support based on a my (Drew) self-defined specification.
*/

package com.example.habitflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.model.UserData
import com.example.habitflow.repository.DataRepository
import com.example.habitflow.repository.HabitRepository
import com.github.mikephil.charting.data.Entry
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class StatsPageViewModel(
    private val dataRepository: DataRepository = DataRepository(),
    private val habitRepository: HabitRepository = HabitRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _totalHabits = MutableStateFlow(0)
    val totalHabits: StateFlow<Int> = _totalHabits.asStateFlow()

    private val _longestStreak = MutableStateFlow(0)
    val longestStreak: StateFlow<Int> = _longestStreak.asStateFlow()

    private val _completionsThisWeek = MutableStateFlow(0)
    val completionsThisWeek: StateFlow<Int> = _completionsThisWeek.asStateFlow()

    private val _dailyCompletions = MutableStateFlow<List<Float>>(emptyList())
    val dailyCompletions: StateFlow<List<Float>> = _dailyCompletions.asStateFlow()

    private val _averageStreaks = MutableStateFlow<List<Float>>(emptyList())
    val averageStreaks: StateFlow<List<Float>> = _averageStreaks.asStateFlow()

    fun loadStats() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            habitRepository.loadHabitsFromFirestore(
                user,
                onSuccess = { habitIds ->
                    if (habitIds.isEmpty()) return@loadHabitsFromFirestore

                    _totalHabits.value = habitIds.size

                    val allUserData = mutableListOf<UserData>()
                    var loaded = 0

                    for (id in habitIds) {
                        habitRepository.getHabitFromFirestore(id) { habit ->
                            val userDataId = habit?.userDataId ?: return@getHabitFromFirestore
                            dataRepository.loadUserDataFromFirestore(userDataId) { result ->
                                result.onSuccess { userData ->
                                    allUserData.add(userData)
                                }
                                loaded++
                                if (loaded == habitIds.size) {
                                    computeStats(allUserData)
                                }
                            }
                        }
                    }
                },
                onFailure = {}
            )
        }
    }

    private fun computeStats(userDataList: List<UserData>) {
        if (userDataList.isEmpty()) return

        _longestStreak.value = userDataList.maxOfOrNull { it.streak } ?: 0

        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, -6) // 7-day window
        val sevenDaysAgo = calendar.time

        val completionsPerDay = IntArray(7) { 0 }
        val streaksPerDay = Array(7) { mutableListOf<Int>() }

        for (userData in userDataList) {
            for (entry in userData.userData) {
                val entryDate = Date(entry.x.toLong())
                if (entryDate.after(sevenDaysAgo)) {
                    val dayIndex = getDayIndex(entryDate)
                    completionsPerDay[dayIndex]++
                    streaksPerDay[dayIndex].add(userData.streak)
                }
            }
        }

        _completionsThisWeek.value = completionsPerDay.sum()
        _dailyCompletions.value = completionsPerDay.map { it.toFloat() }
        _averageStreaks.value = streaksPerDay.map { dayStreaks ->
            if (dayStreaks.isNotEmpty()) dayStreaks.average().toFloat() else 0f
        }
    }

    private fun getDayIndex(date: Date): Int {
        val today = Calendar.getInstance()
        today.time = Date()

        val target = Calendar.getInstance()
        target.time = date

        val diff = ((today.time.time - target.time.time) / (1000 * 60 * 60 * 24)).toInt()
        return 6 - diff.coerceIn(0, 6)
    }
}
