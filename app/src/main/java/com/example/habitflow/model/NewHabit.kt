package com.example.habitflow.model

data class NewHabit(
	var id: String = "",
	val name: String = "",
	val description: String = "",
	val duration: Int = 0,
	val goalAmount: Float = 0f,
	val precision: String = "",
	val goalData: List<Map<String, Any>> = emptyList(),
	val remindersEnabled: Boolean = false,
	val reminderFrequency: String? = null,
	val category: String = "",
	val frequency: String = "",
	val customReminderValue: String? = null,
	val customReminderUnit: String? = null,
	var notificationTriggered: Boolean? = false,
	var userDataId: String = ""
)
