package com.example.habitflow.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.habitflow.repository.DataRepository
import com.example.habitflow.repository.HabitRepository
import com.google.firebase.auth.FirebaseAuth

class StatsPageViewModelFactory(
    private val dataRepository: DataRepository,
    private val habitRepository: HabitRepository,
    private val auth: FirebaseAuth
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsPageViewModel::class.java)) {
            return StatsPageViewModel(dataRepository, habitRepository, auth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
