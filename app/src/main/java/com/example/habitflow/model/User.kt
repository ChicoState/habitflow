package com.example.habitflow.model

data class User(
	val name: String = "",
	val age: Int = 0,
	val gender: String = "",
	val email: String = "",
	val habits: List<String> = emptyList(),
	val pastHabits: List<String> = emptyList()
)