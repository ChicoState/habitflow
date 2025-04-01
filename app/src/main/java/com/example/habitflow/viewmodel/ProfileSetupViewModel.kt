package com.example.habitflow.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.habitflow.repository.AuthRepository

class ProfileSetupViewModel(
	private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

	var name by mutableStateOf("")
	var age by mutableStateOf("")
	var gender by mutableStateOf("")
	var error by mutableStateOf<String?>(null)

	fun validateAndSave(uid: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
		val parsedAge = age.toIntOrNull()
		if (name.isBlank() || parsedAge == null || gender.isBlank()) {
			error = "All fields must be filled correctly."
			return
		}

		repository.saveProfileData(
			uid = uid,
			name = name,
			age = parsedAge,
			gender = gender,
			onSuccess = onSuccess,
			onFailure = onFailure
		)
	}
}
