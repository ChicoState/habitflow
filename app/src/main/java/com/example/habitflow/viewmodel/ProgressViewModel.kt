package com.example.habitflow.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.model.Habit
import com.example.habitflow.model.UserData
import com.example.habitflow.repository.DataRepository
import com.example.habitflow.repository.HabitRepository
import com.github.mikephil.charting.data.Entry
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProgressViewModel(
	private val habitRepository: HabitRepository = HabitRepository,
	private val dataRepository: DataRepository = DataRepository()
) : ViewModel() {

	private val _habit = MutableStateFlow<Habit?>(null)
	private val _userData = MutableStateFlow<UserData?>(null)
	val habit: StateFlow<Habit?> = _habit.asStateFlow()
	val userData: StateFlow<UserData?> = _userData.asStateFlow()

	fun loadHabit(habitId: String) {
		viewModelScope.launch {
			habitRepository.getHabitFromFirestore(habitId) { fetchedHabit ->
				_habit.value = fetchedHabit
			}
		}
	}

	fun loadUserData(userDataId: String) {
		viewModelScope.launch {
			dataRepository.loadUserDataFromFirestore(userDataId) { result ->
				result.onSuccess { fetchedData ->
					_userData.value = fetchedData
				}
				result.onFailure { exception ->
					Log.e("ProgressViewModel", "Error loading user data: ${exception.message}")
				}
			}
		}
	}

	fun getHabitName(): String = _habit.value?.name ?: ""
	fun getStreak(): Int = _userData.value?.streak ?: 0
	fun getDeadline(): Timestamp? = _userData.value?.deadlineAsTimestamp


	private val _span = MutableStateFlow("Overall")
	val span: StateFlow<String> = _span.asStateFlow()
	fun setSpan(newSpan: String) {
		_span.value = newSpan
	}

	val spanData: StateFlow<List<Entry>> = _span
		.combine(_userData) { span, data ->
			val fullData = data?.userData ?: return@combine emptyList()
			val now = System.currentTimeMillis()
			val cutoff = when (span) {
				"Weekly" -> now - 7 * 24 * 60 * 60 * 1000L
				"Monthly" -> now - 30 * 24 * 60 * 60 * 1000L
				else -> Long.MIN_VALUE
			}
			fullData.filter { it.x >= cutoff }
		}.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

	fun getSpanButtons(habitId: String, userDataId: String): List<Pair<String, String>> {
		return listOf("Weekly", "Monthly", "Overall").map { span ->
			val route = "progress/$habitId/$userDataId/$span"
			span to route
		}
	}

	fun getUserProgress() : Float? {
		val data = _userData.value
		return if (data?.progressPercentage != null) {
			when (data.progressPercentage > 100f) {
				true -> 100f
				false -> data.progressPercentage
			}
		} else 0f
	}

	fun getDecreasing() : Boolean? {
		val data = _userData.value
		if (data != null) {
			return data.calculateDecreasing(data.userData)
		}
		return null
	}

	fun getIncreasing() : Boolean? {
		val data = _userData.value
		if (data != null) {
			return data.calculateIncreasing(data.userData)
		}
		return null
	}

	fun getTodayValue(): Float? {
		return _userData.value?.userData?.lastOrNull()?.y
	}

	fun shouldPromptEntry(): Boolean {
		return _userData.value?.promptEntry == false
	}

	fun getEncouragementMessage(): String {
		val userData = _userData.value ?: return ""
		val type = userData.type
		val dec = getDecreasing()
		val inc = getIncreasing()

		return when {
			dec == true && type == "good" -> "Let's work on improving this habit!"
			inc == true && type == "bad" -> "Let's work on improving this habit!"
			inc == true && type == "good" -> "Keep up the great work!"
			dec == false && type == "bad" -> "Keep up the great work!"
			else -> "You're off to a consistent start. Keep going!"
		}
	}
}
