package com.example.habitflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.model.Habit
import com.example.habitflow.repository.HabitRepository
import com.github.mikephil.charting.data.Entry
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

	fun countDaysWithLargerY(list1: List<Entry>, list2: List<Entry>): Int {
		// Find the minimum size to avoid IndexOutOfBoundsException
		val minSize = minOf(list1.size, list2.size)
		var count = 0

		for (i in 0 until minSize) { // Loop through both lists
			if (list1[i].y > list2[i].y) { // Compare the y values
				count++
			}
		}

		return count
	}

	fun countDaysWithSmallerY(list1: List<Entry>, list2: List<Entry>): Int {
		// Find the minimum size to avoid IndexOutOfBoundsException
		val minSize = minOf(list1.size, list2.size)
		var count = 0

		for (i in 0 until minSize) { // Loop through both lists
			if (list1[i].y < list2[i].y) { // Compare the y values for smaller values
				count++
			}
		}

		return count
	}

	fun countMatchingFromEndBad(list1: List<Entry>, list2: List<Entry>): Int {
		val minSize = minOf(list1.size, list2.size) // Find the smaller list size
		var count = 0

		for (i in 1..minSize) { // Loop from end to start
			if (list1[list1.size - i].y <= list2[list1.size - i].y) {
				count++
			} else {
				break // Stop counting when a mismatch occurs
			}
		}

		return count
	}

	fun countMatchingFromEndGood(list1: List<Entry>, list2: List<Entry>): Int {
		val minSize = minOf(list1.size, list2.size) // Find the smaller list size
		var count = 0

		for (i in 1..minSize) { // Loop from end to start
			if (list1[list1.size - i].y >= list2[list1.size - i].y) {
				count++
			} else {
				break // Stop counting when a mismatch occurs
			}
		}

		return count
	}

	fun compareLists(list1: List<Entry>, list2: List<Entry>): List<Entry> {
		val resultList = mutableListOf<Entry>()

		// Iterate through the indices of both lists
		for (i in list1.indices) {
			// Ensure both lists have the same index length and valid entry
			if (i < list2.size) {
				val entry1 = list1[i]
				val entry2 = list2[i]

				// Check if the y components are equal for the same x index
				if (entry1.y == entry2.y) {
					resultList.add(entry1) // Add the entry from list1 (or list2, they have the same y value)
				}
			}
		}

		return resultList
	}
}
