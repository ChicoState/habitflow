package com.example.habitflow.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.habitflow.repository.HabitRepository
import com.google.firebase.auth.FirebaseAuth

class HomeViewModelFactory(
	private val application: Application,
	private val habitRepository: HabitRepository,
	private val auth: FirebaseAuth
) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
			return HomeViewModel(application, habitRepository, auth) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}
