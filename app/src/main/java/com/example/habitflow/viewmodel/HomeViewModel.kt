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

	private val _habits = MutableStateFlow<Map<Habit, UserData?>>(emptyMap())
	val habits: StateFlow<Map<Habit, UserData?>> = _habits.asStateFlow()

	fun loadHabits() {
		val user = auth.currentUser ?: return
		viewModelScope.launch {
			habitRepository.loadHabitsFromFirestore(
				user = user,
				onSuccess = { habitIds ->
					val loadedHabits = mutableListOf<Habit>()
					var remaining = habitIds.size
					if (habitIds.isEmpty()) {
						_habits.value = emptyMap() // Empty map if no habits
						return@loadHabitsFromFirestore
					}

					habitIds.forEach { id ->
						habitRepository.getHabitFromFirestore(id) { habit ->
							habit?.let { loadedHabits.add(it) }
							remaining--

							// Once all habits are loaded, load userData for each habit
							if (remaining == 0) {
								val habitsWithUserData = mutableMapOf<Habit, UserData?>()

								// Load user data for each habit
								loadedHabits.forEach { hab ->
									loadUserDataForHabit(hab) { userData ->
										habitsWithUserData[hab] = userData

										// Once all user data is loaded, sort the map
										if (habitsWithUserData.size == loadedHabits.size) {
											// Sort the habits based on the `createDate`
											val sortedHabits = habitsWithUserData.entries
												.sortedBy { it.key.createDate.toDate() }
												.associate { it.key to it.value }

											// Update the state with the sorted map
											_habits.value = sortedHabits
										}
									}
								}
							}
						}
					}
				},
				onFailure = { _habits.value = emptyMap() }
			)
		}
	}

	// Fetch the userData for each habit and return it via a callback
	private fun loadUserDataForHabit(habit: Habit, onComplete: (UserData?) -> Unit) {
		viewModelScope.launch {
			val userDataId = habit.userDataId
			if (userDataId.isEmpty()) {
				onComplete(null)
			} else {
				dataRepository.loadUserDataFromFirestore(
					userDataId = userDataId,
					onSuccess = { loadedUserData ->
						onComplete(loadedUserData) // Pass userData via callback
					},
					onFailure = { errorMessage ->
						Log.e("HomeViewModel", "Error loading user data for habit ${habit.id}: $errorMessage")
						onComplete(null)
					}
				)
			}
		}
	}

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
				// Call deleteUserData for each userDataId
				dataRepository.deleteUserData(userDataId) { _ ->
					completedCount++

					if (completedCount == totalToDelete) {
						// delete habits
						proceedToDeleteHabits(habitIds, user, onComplete)
					}
				}
			}
		} else {
			// No userDataIds to delete, proceed directly with habit deletion
			proceedToDeleteHabits(habitIds, user, onComplete)
		}
	}

	// Helper function to delete habits and update local state
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
				// Remove the deleted habits from the _habits map
				_habits.value = _habits.value.filterKeys { habit -> !habitIds.contains(habit.id) }
				onComplete()
			}
		)
	}



}
