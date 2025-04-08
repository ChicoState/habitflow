package com.example.habitflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.model.Habit
import com.example.habitflow.repository.HabitRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
	application: Application,
	private val habitRepository: HabitRepository = HabitRepository,
	private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AndroidViewModel(application) {

	private val _habits = MutableStateFlow<List<Habit>>(emptyList())
	val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

	fun loadHabits() {
		val user = auth.currentUser ?: return
		viewModelScope.launch {
			habitRepository.loadHabitsFromFirestore(
				user = user,
				onSuccess = { habitIds ->
					val loaded = mutableListOf<Habit>()
					var remaining = habitIds.size
					if (habitIds.isEmpty()) {
						_habits.value = emptyList()
						return@loadHabitsFromFirestore
					}
					habitIds.forEach { id ->
						habitRepository.getHabitFromFirestore(id) { habit ->
							habit?.let { loaded.add(it) }
							remaining--
							if (remaining == 0) {
								val sortedHabits = loaded.sortedBy { it.createDate.toDate() }
								_habits.value = sortedHabits
							}
						}
					}
				},
				onFailure = { _habits.value = emptyList() }
			)
		}
	}

	fun moveToPastHabits(habitIds: Set<String>, onComplete: () -> Unit) {
		onComplete()
	}

	fun deleteHabits(habitIds: Set<String>, onComplete: () -> Unit) {
		val user = auth.currentUser ?: return
		habitRepository.deleteHabitsForUser(
			context = getApplication(),  // you need to pass context
			user = user,
			habitIds = habitIds,
			onComplete = {
				val currentList = _habits.value
				_habits.value = currentList.filterNot { habitIds.contains(it.id) }
				onComplete()
			}
		)
	}

}
