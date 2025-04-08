package com.example.habitflow.model

import com.google.firebase.Timestamp

data class NewHabit(
	var id: String = "",
	val name: String = "",
	val description: String = "",
	val type: String = "",  // "good" or "bad"
	val duration: Int = 0,
	val goalAmount: Float = 0f,
	val precision: String = "",
	val goalData: List<Map<String, Any>> = emptyList(),
	val remindersEnabled: Boolean = false,
	val reminderFrequency: String? = null,
	val trackingMethod: String = "",  // "binary" (Yes/No), "numeric" (Count/Quantity), or "timeBased" (Time Spent)
	val category: String = "",          // e.g., "Health", "Productivity"
	val frequency: String = "",         // e.g., "Daily", "3 times per week"
	val deadline: String? = null,       // ISO string or a formatted date; if null then ongoing
	val startDate: String? = null,
	val customReminderValue: String? = null,
	val customReminderUnit: String? = null,
	var notificationTriggered: Boolean? = true,
	var createDate: Timestamp = Timestamp.now(),
	var userDataId: String = ""
	//val userDataId: String,  // The user ID who created the habit

)
