package com.example.habitflow.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.model.Habit
import com.example.habitflow.model.UserData
import com.example.habitflow.repository.DataRepository
import com.example.habitflow.repository.HabitRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
	application: Application,
	private val dataRepository: DataRepository = DataRepository(),
	private val habitRepository: HabitRepository = HabitRepository,
	private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AndroidViewModel(application) {

	private val _habits = MutableStateFlow<Map<Habit, UserData>>(emptyMap())
	val habits: StateFlow<Map<Habit, UserData>> = _habits.asStateFlow()

	fun loadHabits() {
		val user = auth.currentUser ?: return
		viewModelScope.launch {
			fetchHabitIds(user)
		}
	}

	// Helper function to loadHabits()
	private fun fetchHabitIds(user: FirebaseUser) {
		habitRepository.loadHabitsFromFirestore(
			user = user,
			onSuccess = { habitIds ->
				if (habitIds.isEmpty()) {
					_habits.value = emptyMap()
					return@loadHabitsFromFirestore
				}
				loadHabitsByIds(habitIds)
			},
			onFailure = { _habits.value = emptyMap() }
		)
	}

	// Helper function to fetchHabitIds()
	private fun loadHabitsByIds(habitIds: List<String>) {
		val loadedHabits = mutableListOf<Habit>()
		var remaining = habitIds.size

		habitIds.forEach { id ->
			habitRepository.getHabitFromFirestore(id) { habit ->
				habit?.let { loadedHabits.add(it) }
				remaining--

				if (remaining == 0) {
					loadUserDataForHabits(loadedHabits)
				}
			}
		}
	}

	private fun loadUserDataForHabits(habits: List<Habit>) {
		val habitsWithUserData = mutableListOf<Pair<Habit, UserData>>()

		habits.forEach { habit ->
			loadUserDataForHabit(habit) { result ->
				result.onSuccess { userData ->
					habitsWithUserData.add(habit to userData)
				}
				result.onFailure { error ->
					Log.e("HomeViewModel", "Error loading user data for habit ${habit.id}: $error")
				}

				if (habitsWithUserData.size == habits.size) {
					commitSortedHabits(habitsWithUserData)
				}
			}
		}
	}

	// Helper function to loafHabitsByIds()
	private fun loadUserDataForHabit(habit: Habit, onComplete: (Result<UserData>) -> Unit) {
		viewModelScope.launch {
			val userDataId = habit.userDataId

			if (userDataId.isBlank() || userDataId == "userData") {
				Log.e("HomeViewModel", "Invalid userDataId for habit ${habit.id}: '$userDataId'")
				onComplete(Result.failure(IllegalArgumentException("Invalid userDataId: '$userDataId'")))
				return@launch
			}

			dataRepository.loadUserDataFromFirestore(
				userDataId = userDataId,
				onComplete = { result ->
					result.onSuccess { loadedUserData ->
						onComplete(Result.success(loadedUserData))
					}
					result.onFailure { error ->
						Log.e("HomeViewModel", "Error loading user data for habit ${habit.id}: ${error.message}")
						onComplete(Result.failure(error))
					}
				}
			)
		}
	}

	// Helper function to loadUserDataForHabits()
	private fun commitSortedHabits(habitsWithUserData: List<Pair<Habit, UserData>>) {
		val sorted = habitsWithUserData
			.sortedBy { it.second.createDate.toDate() }
			.associate { it.first to it.second }

		_habits.value = sorted
	}

		// Helper function to loadUserDataForHabit()
	fun moveToPastHabits(habitIds: Set<String>, onComplete: () -> Unit) {
		onComplete()
	}

	fun deleteHabits(habitIds: Set<String>, onComplete: () -> Unit) {
		val user = auth.currentUser ?: return

		// Extract the habits to delete from the map, based on habitIds
		val habitsToDelete = _habits.value.filterKeys { habitIds.contains(it.id) }

		// Extract valid userDataIds
		val userDataIds = habitsToDelete.mapNotNull { (habit, _) ->
			habit.userDataId.takeIf { it.isNotBlank() }
		}

		// If there are userDataIds, delete them first
		if (userDataIds.isNotEmpty()) {
			var completedCount = 0
			val totalToDelete = userDataIds.size

			userDataIds.forEach { userDataId ->
				dataRepository.deleteUserData(userDataId) { _ ->
					completedCount++

					if (completedCount == totalToDelete) {
						proceedToDeleteHabits(habitIds, user, onComplete)
					}
				}
			}
		} else {
			proceedToDeleteHabits(habitIds, user, onComplete)
		}
	}

	// Helper function to deleteHabits()
	private fun proceedToDeleteHabits(
		habitIds: Set<String>,
		user: FirebaseUser,
		onComplete: () -> Unit
	) {
		habitRepository.deleteHabitsForUser(
			context = getApplication(),
			user = user,
			habitIds = habitIds,
			onComplete = {
				_habits.value = _habits.value.filterKeys { habit -> !habitIds.contains(habit.id) }
				onComplete()
			}
		)
	}
}
