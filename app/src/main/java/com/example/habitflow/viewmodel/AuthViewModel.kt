package com.example.habitflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
	private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

	private val _signUpState = MutableStateFlow<Result<String>?>(null)
	val signUpState: StateFlow<Result<String>?> = _signUpState

	private val _loginState = MutableStateFlow<Result<Unit>?>(null)
	val loginState: StateFlow<Result<Unit>?> = _loginState

	fun signUp(email: String, password: String) {
		viewModelScope.launch {
			val result = repository.signUp(email, password)
			_signUpState.value = result
		}
	}

	fun loginUser(email: String, password: String) {
		viewModelScope.launch {
			val result = repository.login(email, password)
			_loginState.value = result
		}
	}

	fun clearLoginState() {
		_loginState.value = null
	}

	fun saveProfileData(
		uid: String,
		name: String,
		age: Int,
		gender: String,
		onSuccess: () -> Unit,
		onFailure: (Throwable) -> Unit
	) {
		repository.saveProfileData(uid, name, age, gender, onSuccess, onFailure)
	}
}
