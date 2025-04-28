package com.example.habitflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.habitflow.repository.HabitRepository
import com.google.firebase.auth.FirebaseAuth

class StatsViewModelFactory(
	private val habitRepository: HabitRepository,
	private val auth: FirebaseAuth
) : ViewModelProvider.Factory {

	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST")
			return StatsViewModel(habitRepository, auth) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}