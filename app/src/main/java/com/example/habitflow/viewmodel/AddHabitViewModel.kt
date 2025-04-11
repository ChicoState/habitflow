package com.example.habitflow.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.habitflow.model.NewHabit
import com.example.habitflow.repository.DataRepository
import com.example.habitflow.repository.HabitRepository
import com.example.habitflow.repository.generateGoalDataForFirestore


class AddHabitViewModel : ViewModel() {

	var habitName by mutableStateOf("")
	var habitDescription by mutableStateOf("")
	var isGoodHabit by mutableStateOf(false)
	var isBadHabit by mutableStateOf(false)
	var duration by mutableIntStateOf(0)
	var reminders by mutableStateOf(false)
	var hourly by mutableStateOf(false)
	var daily by mutableStateOf(false)
	var weekly by mutableStateOf(false)
	var custom by mutableStateOf(false)
	var endAmount by mutableFloatStateOf(0f)
	var precision by mutableStateOf("")
	var wholeNumbers by mutableStateOf(false)
	var tenths by mutableStateOf(false)
	var hundredths by mutableStateOf(false)
	var category by mutableStateOf("General")
	private var frequency by mutableStateOf("")
	var deadline by mutableStateOf("")
	var customReminderValue by mutableStateOf("")
	var customReminderUnit by mutableStateOf("Days")
	var showDescriptionField by mutableStateOf(false)
	var trackingMethodLabel by mutableStateOf("Yes/No")

	fun applyPrecision(value: String) {
		precision = value
		wholeNumbers = value == "wholeNumbers"
		tenths = value == "tenths"
		hundredths = value == "hundredths"
	}
	private val goalData: List<Map<String, Any>>
		get() = generateGoalDataForFirestore(duration, endAmount, precision)

	fun saveHabitToFirestore(context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
		val user = FirebaseAuth.getInstance().currentUser ?: return onFailure("No user found")

		val userDataRepository = DataRepository()
		val habitRepository = HabitRepository

		val trackingMethod = when (trackingMethodLabel) {
			"Yes/No" -> "binary"
			"Count/Quantity" -> "numeric"
			"Time Spent" -> "timeBased"
			else -> "binary"
		}
		val newDeadline = deadline.ifBlank { null }

		userDataRepository.createEmptyUserData(isGoodHabit, newDeadline, trackingMethod, onSuccess = { userDataId ->
			val newHabit = NewHabit(
				name = habitName,
				description = habitDescription,
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
				category = category,
				frequency = frequency,
				customReminderValue = customReminderValue.takeIf { it.isNotBlank() },
				customReminderUnit = customReminderUnit.takeIf { it.isNotBlank() },
				userDataId = userDataId,
				notificationTriggered = true
			)

			habitRepository.createHabitForUser(
				context = context,
				user = user,
				habit = newHabit,
				onSuccess = onSuccess,
				onFailure = onFailure,
			)
		},
			onFailure = { errorMessage ->
				onFailure(errorMessage)
			}
		)
	}
}