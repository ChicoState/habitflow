package com.example.habitflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.Habit
import com.example.habitflow.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgressViewModel(
	private val habitRepository: HabitRepository = HabitRepository
) : ViewModel() {

	private val _habit = MutableStateFlow<Habit?>(null)
	val habit: StateFlow<Habit?> = _habit.asStateFlow()

	fun loadHabit(habitId: String) {
		viewModelScope.launch {
			habitRepository.getHabitFromFirestore(habitId) { fetchedHabit ->
				_habit.value = fetchedHabit
			}
		}
	}
}
