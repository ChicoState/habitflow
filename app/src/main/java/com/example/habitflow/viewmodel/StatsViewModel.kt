package com.example.habitflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.model.Habit
import com.example.habitflow.repository.HabitRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StatsViewModel(
	private val habitRepository: HabitRepository,
	private val auth: FirebaseAuth
) : ViewModel() {

	private val _habits = MutableStateFlow<List<Habit>>(emptyList())
	val habits: StateFlow<List<Habit>> = _habits

	private val _isLoading = MutableStateFlow(true)
	val isLoading: StateFlow<Boolean> = _isLoading

	init {
		loadHabits()
	}

	private fun loadHabits() {
		val user = auth.currentUser
		if (user == null) {
			_isLoading.value = false
			return
		}

		_isLoading.value = true
		habitRepository.loadFullHabitsForUser(
			user,
			onSuccess = { habitsList ->
				_habits.value = habitsList
				_isLoading.value = false
			},
			onFailure = {
				_habits.value = emptyList()
				_isLoading.value = false
			}
		)
	}
}