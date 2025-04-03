package com.example.habitflow.viewmodel

import androidx.lifecycle.ViewModel
import com.example.habitflow.model.User
import com.example.habitflow.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
	private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

	private val _user = MutableStateFlow<User?>(null)
	val user: StateFlow<User?> = _user

	fun fetchUserData() {
		repository.fetchUserData { fetchedUser ->
			_user.value = fetchedUser
		}
	}

	fun updateUserData(
		updatedUser: User,
		onSuccess: () -> Unit,
		onFailure: (Exception) -> Unit
	) {
		repository.updateUserData(updatedUser, onSuccess, onFailure)
	}

	fun deleteUserAccount(
		onSuccess: () -> Unit,
		onFailure: (Exception) -> Unit
	) {
		repository.deleteUserAccount(onSuccess, onFailure)
	}
}
