package com.example.habitflow.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.habitflow.model.NewHabit
import com.example.habitflow.repository.generateGoalDataForFirestore


class AddHabitViewModel : ViewModel() {

	var habitName by mutableStateOf("")
	var habitDescription by mutableStateOf("")
	var isGoodHabit by mutableStateOf(false)
	var isBadHabit by mutableStateOf(false)
	var duration by mutableStateOf(0)
	var reminders by mutableStateOf(false)
	var texts by mutableStateOf(false)
	var hourly by mutableStateOf(false)
	var daily by mutableStateOf(false)
	var weekly by mutableStateOf(false)
	var custom by mutableStateOf(false)
	var endAmount by mutableStateOf(0f)
	var precision by mutableStateOf("")
	var wholeNumbers by mutableStateOf(false)
	var tenths by mutableStateOf(false)
	var hundredths by mutableStateOf(false)
	var trackingMethod by mutableStateOf("binary")
	var category by mutableStateOf("General")
	var frequency by mutableStateOf("")
	var deadline by mutableStateOf("")
	var startDate by mutableStateOf("")
	var customReminderValue by mutableStateOf("")
	var customReminderUnit by mutableStateOf("Days") // or "Hours"
	var customInterval by mutableStateOf(1)
	var showDescriptionField by mutableStateOf(false)
	var trackingMethodLabel by mutableStateOf("Yes/No")

	fun applyPrecision(value: String) {
		precision = value
		wholeNumbers = value == "wholeNumbers"
		tenths = value == "tenths"
		hundredths = value == "hundredths"
	}
	// Derived property for goal data:
	val goalData: List<Map<String, Any>>
		get() = generateGoalDataForFirestore(duration, endAmount, precision)

	fun saveHabitToFirestore(context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
		val user = FirebaseAuth.getInstance().currentUser ?: return onFailure("No user found")

		// Use the derived goalData instead of recalculating here:
		val newHabit = NewHabit(
			name = habitName,
			description = habitDescription,
			type = if (isGoodHabit) "good" else "bad",
			duration = duration,
			goalAmount = endAmount,
			precision = precision,
			goalData = goalData,
			remindersEnabled = reminders,
			reminderFrequency = when {
				hourly -> "hourly"
				daily -> "daily"
				weekly -> "weekly"
				custom -> "custom"
				else -> null
			},
			trackingMethod = when (trackingMethodLabel) {
				"Yes/No" -> "binary"
				"Count/Quantity" -> "numeric"
				"Time Spent" -> "timeBased"
				else -> "binary"
			},
			category = category,
			frequency = frequency,
			deadline = if (deadline.isNotBlank()) deadline else null,
			startDate = if (startDate.isNotBlank()) startDate else null,
			customReminderValue = customReminderValue.takeIf { it.isNotBlank() },
			customReminderUnit = customReminderUnit.takeIf { it.isNotBlank() },
		)


		// Pass newHabit to your repository method
		com.example.habitflow.repository.HabitRepository.createHabitForUser(
			context = context,
			user = user,
			habit = newHabit,
			onSuccess = onSuccess,
			onFailure = onFailure
		)
	}
}